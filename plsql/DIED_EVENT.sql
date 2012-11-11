create or replace 
PROCEDURE DIED_EVENT( mobDBID IN INT, way IN VARCHAR2, locX IN INT, locY in INT, locZ IN INT ) 
  AS 

  eventID INT;
  decisionID INT;
  locID INT;
  choiceID INT;
   
   cursor currEvents is
   select event_hist_id
     from event_hist_new
     where obj_id = mobDBID;
  
  BEGIN

   -- Only allow some input
   IF way NOT IN ( 'Killed','Starved' ) THEN RETURN; END IF;
   
   -- get the 'empty' choice
  choiceID := return_ecm_match (' ',' ',' ',' ',' ',' ',' ');

  locID := return_loc_match(locX, locY, LocZ);
  
  IF locID is null
  THEN
  
    INSERT INTO EVENT_LOC ( EVENT_LOC_ID, LOC_X, LOC_Y, LOC_Z )
      SELECT 0, locX, locY, locZ FROM DUAL;
    
    SELECT MAX(event_loc_id) 
      INTO locID
      FROM EVENT_LOC;
  
  END IF;
  
  decisionID := return_decision_match ( mobDBID, choiceID, locID );
  
  IF decisionID is null 
  THEN

    INSERT INTO EVENT_DECISION ( EVENT_DECISION_ID, OBJ_ID, EVENT_CHOICE_ID, EVENT_LOC_ID ) 
      SELECT 0, 
             mobDBID, 
             choiceID,
             locID
        FROM DUAL;
      
    SELECT max(EVENT_DECISION_ID) 
      INTO decisionID
      FROM EVENT_DECISION;

  END IF;

  INSERT INTO EVENT_HIST ( EVENT_HIST_ID, EVENT_DECISION_ID, EVENT_TYPE, CHOICE_FACING ) 
    SELECTf 0, decisionID, way, 0 FROM DUAL;

  -- They only learn that the last n < 50 moves leading to starvation were bad
  IF way = 'Starved' THEN
  
    SELECT max(EVENT_HIST_ID) 
      INTO eventID
      FROM EVENT_HIST;
  
    -- Enter the top(chainCount) rows into the naughty table, except the 1st
    INSERT INTO EVENT_SCORING ( EVENT_HIST_ID )
      SELECT eh.event_hist_id
        FROM event_hist eh
        JOIN event_decision ed  ON eh.event_decision_id = ed.event_decision_id
        JOIN event_hist_new ehn ON eh.event_hist_id = ehn.event_hist_id
                               AND ed.obj_id        = ehn.obj_id
      WHERE ed.obj_id = mobDBID;
          
  ELSE
   
    -- If a mob is killed, remove the memories from the buffer and event_hist
    --   This is because being killed is so arbitrary that it shouldn't affect the decision trees
     
    FOR delete_event_id in currEvents
    LOOP
      DELETE FROM event_hist_new where event_hist_id = delete_event_id.event_hist_id;
      DELETE FROM event_hist where event_hist_id = delete_event_id.event_hist_id;
    END LOOP;
  
  END IF;

     -- Clears out the new decisions for this jobject
  DELETE FROM event_hist_new 
   WHERE obj_id = mobDBID;
     
END DIED_EVENT;