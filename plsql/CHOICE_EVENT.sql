create or replace 
FUNCTION CHOICE_EVENT( mobDBID IN INT,
                                  locX IN INT,
                                  locY IN INT,
                                  locZ IN INT,
                                  target0 IN VARCHAR,
                                  target1 IN VARCHAR,
                                  target2 IN VARCHAR,
                                  target3 IN VARCHAR,
                                  target4 IN VARCHAR,
                                  target5 IN VARCHAR,
                                  target6 IN VARCHAR ) 
  RETURN INT
  AS 

  decisionID INT;
  choiceID INT;
  facingID INT;
  mobTeam VARCHAR2(10);
  
  dirX INT;
  dirY INT;
  dirZ INT;

  BEGIN

  choiceID := return_ecm_match(target0, target1, target2, target3, target4, target5, target6 );
  
  -- If nothing similar, create new node and use it's EVENT_CHOICE_ID
  IF choiceID IS NULL
    THEN     
      INSERT INTO event_choice_node ( event_choice_id, ct0, ct1, ct2, ct3, ct4, ct5, ct6 )
        SELECT 0, nvl(target0,' '), nvl(target1,' '), nvl(target2,' '), nvl(target3,' '), nvl(target4,' '), nvl(target5,' '), nvl(target6,' ')
          FROM dual;
      
        SELECT max(event_choice_id)
          INTO choiceID
          FROM event_choice_node;

        -- Create all the applicable rows in EVENT_CHOICE
        for i in 0..6 loop
            
            IF ( i=0 and target0 IS NOT NULL ) OR
               ( i=1 and target1 IS NOT NULL ) OR
               ( i=2 and target2 IS NOT NULL ) OR
               ( i=3 and target3 IS NOT NULL ) OR
               ( i=4 and target4 IS NOT NULL ) OR
               ( i=5 and target5 IS NOT NULL ) OR
               ( i=6 and target6 IS NOT NULL ) THEN 
            
                select locX into dirX from dual;
                select locY into dirY from dual;
                select locZ into dirZ from dual;
                
                -- Minus first!
                IF i = 1 THEN select dirX - 1 into dirX from dual; END IF;
                IF i = 2 THEN select dirX + 1 into dirX from dual; END IF;
                IF i = 3 THEN select dirY - 1 into dirY from dual; END IF;
                IF i = 4 THEN select dirY + 1 into dirY from dual; END IF;
                IF i = 5 THEN select dirZ - 1 into dirZ from dual; END IF;
                IF i = 6 THEN select dirZ + 1 into dirZ from dual; END IF;
                
                insert into event_choice ( EVENT_CHOICE_ID, CHOICE_FACING, CHOICE_TARGET, CHOICE_ORIG_X, CHOICE_ORIG_Y, CHOICE_ORIG_Z, CHOICE_DIR_X, CHOICE_DIR_Y, CHOICE_DIR_Z )
                  select choiceID, i, CASE i WHEN 0 THEN target0
                                             WHEN 1 THEN target1
                                             WHEN 2 THEN target2
                                             WHEN 3 THEN target3
                                             WHEN 4 THEN target4
                                             WHEN 5 THEN target5
                                             WHEN 6 THEN target6 END CASE, 
                         locX, locY, locZ, dirX, dirY, dirZ
                    from dual;
                                          
            END IF;
        end loop;
  END IF;
      
        -- insert new decision
  INSERT INTO event_decision ( event_decision_id, obj_id, event_choice_id ) 
      SELECT 0, mobDBID, choiceID FROM DUAL;
  
  SELECT MAX(event_decision_id)
    INTO decisionID
    FROM event_decision;
  
   SELECT obj_team
     INTO mobTeam
     FROM obj
    WHERE obj_id = mobDBID;

  facingID := get_best_choice(mobTeam,choiceID);
  
  -- If no move is preferred randomize
  IF facingID is null 
  THEN 
   SELECT choice_facing
   INTO facingID
   FROM ( SELECT choice_facing, dbms_random.value() 
            FROM event_choice
           WHERE event_choice_id = choiceID
           ORDER by 2 )
   WHERE ROWNUM = 1;
  END IF;
  
  SELECT choice_target
    INTO mobTeam
    FROM event_choice
   WHERE event_choice_id = choiceID AND
         choice_facing = facingID;
  
        -- insert event with choice taken
  INSERT INTO EVENT_HIST ( EVENT_HIST_ID, EVENT_DECISION_ID, EVENT_TYPE, CHOICE_FACING ) 
      SELECT 0, decisionID, mobTeam, facingID FROM DUAL;

  RETURN facingID;
  
END CHOICE_EVENT;