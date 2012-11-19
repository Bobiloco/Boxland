create or replace 
FUNCTION CHOICE_EVENT( mobDBID IN INTEGER,
                        locX IN INTEGER,
                        locY IN INTEGER,
                        locZ IN INTEGER,
                        target0 IN CHAR,
                        target1 IN CHAR,
                        target2 IN CHAR,
                        target3 IN CHAR,
                        target4 IN CHAR,
                        target5 IN CHAR,
                        target6 IN CHAR ) 
  RETURN INTEGER
  AS 

-- Boxland - Bernard McManus 2012
-- Choice_Event.sql - Creates rows for a decision in the database

  eventID     INTEGER;
  decisionID  INTEGER;
  locID       INTEGER;
  choiceID    INTEGER;
  facingID    INTEGER;
  mobTeam     CHAR(10);
  lastFacing  INTEGER;
  
  BEGIN

  choiceID := return_ecm_match(target0, target1, target2, target3, target4, target5, target6 );
  
  -- If nothing similar, create new node and use it's EVENT_CHOICE_ID
  IF choiceID IS NULL
    THEN     
      INSERT INTO event_choice_node ( event_choice_id, ct0, ct1, ct2, ct3, ct4, ct5, ct6 )
        SELECT 0, nvl(target0,' '), nvl(target1,' '), nvl(target2,' '), nvl(target3,' '), nvl(target4,' '), nvl(target5,' '), nvl(target6,' ')
          FROM dual;
      
        --SELECT max(event_choice_id) INTO choiceID FROM event_choice_node;
        choiceID := event_choice_seq.currval;

        -- Create all the applicable rows in EVENT_CHOICE
        -- This is ugly now that I've pulled the xyz... still works, though
        for i in 0..6 loop
            
            IF ( i=0 and target0 NOT IN ('Ground','Air') ) OR
               ( i=1 and target1 NOT IN ('Ground','Air') ) OR
               ( i=2 and target2 NOT IN ('Ground','Air') ) OR
               ( i=3 and target3 NOT IN ('Ground','Air') ) OR
               ( i=4 and target4 NOT IN ('Ground','Air') ) OR
               ( i=5 and target5 NOT IN ('Ground','Air') ) OR
               ( i=6 and target6 NOT IN ('Ground','Air') ) 
            THEN
                insert into event_choice ( EVENT_CHOICE_ID, CHOICE_FACING, CHOICE_TARGET )
                  select choiceID, i, CASE i WHEN 0 THEN target0
                                             WHEN 1 THEN target1
                                             WHEN 2 THEN target2
                                             WHEN 3 THEN target3
                                             WHEN 4 THEN target4
                                             WHEN 5 THEN target5
                                             WHEN 6 THEN target6 
                                         END CASE
                    from dual;
                                          
            END IF;
        end loop;
  END IF;

  -- Set location
  locID := return_loc_match(locX, locY, LocZ);
  IF locID is null
  THEN
  
    INSERT INTO EVENT_LOC ( EVENT_LOC_ID, LOC_X, LOC_Y, LOC_Z )
      SELECT 0, locX, locY, locZ FROM DUAL;
    
    locID := event_loc_seq.currval;
    -- SELECT MAX(event_loc_id) INTO locID FROM EVENT_LOC;
  
  END IF;
  
 -- Get the last action taken by this mob, 
  --   or set to -3 if null ( -1 and -2 are also okay. )
  lastFacing := return_last_facing( mobDBID );
  IF lastFacing IS NULL THEN lastFacing := -3; END IF;

  -- Set facing
  facingID := get_best_facing(mobDBID, choiceID, locID, lastFacing );

  -- If no move is preferred randomize
  IF facingID is null 
  THEN 
   SELECT choice_facing
   INTO facingID
   FROM ( SELECT choice_facing, dbms_random.value() rand
            FROM event_choice
           WHERE event_choice_id = choiceID
           ORDER by 2 )
   WHERE ROWNUM = 1;
  END IF;
  
  -- Set target ( reusing mobTeam )
  SELECT choice_target
    INTO mobTeam
    FROM event_choice
   WHERE event_choice_id = choiceID AND
         choice_facing = facingID;
  
  -- See if that mob has made a similar decision
  decisionID := return_decision_match(mobDBID, choiceID, locID, lastFacing);       
  IF decisionID IS NULL THEN
      -- insert new decision
      INSERT INTO event_decision ( event_decision_id, obj_id, event_choice_id, event_loc_id, last_facing ) 
          SELECT 0, mobDBID, choiceID, locID, lastFacing FROM DUAL;
      decisionID := event_decision_seq.currval;
      -- SELECT MAX(event_decision_id) INTO decisionID FROM event_decision;
   END IF;

  -- insert event with choice taken
  INSERT INTO EVENT_HIST ( EVENT_HIST_ID, EVENT_DECISION_ID, EVENT_TYPE, CHOICE_FACING ) 
      SELECT 0, decisionID, mobTeam, facingID FROM DUAL;
  eventID := event_hist_seq.currval;
  -- SELECT MAX(event_hist_id) INTO eventID FROM event_hist;
  
  INSERT INTO EVENT_HIST_NEW ( obj_id, event_hist_id )
    SELECT mobDBID, eventID FROM DUAL;

  -- Try to keep the list trim
  SELECT COUNT(*) 
    INTO eventID
    FROM EVENT_HIST_NEW
   WHERE obj_id = mobDBID;
   
  IF eventID > 49 THEN
    
    SELECT MIN(EVENT_HIST_ID)
      INTO eventID
      FROM event_hist_new
     WHERE obj_id = mobDBID;
    
    -- By deleting this memory from the buffer with no _scoring record, 
    --  it is saved as a good memory - a choice from that position that lead to survival
    DELETE FROM event_hist_new WHERE event_hist_id = eventID;

  END IF;

  RETURN facingID;
  
END CHOICE_EVENT;