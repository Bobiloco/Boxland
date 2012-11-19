CREATE OR REPLACE
PROCEDURE DIED_EVENT( mobDBID IN INTEGER, 
                          way IN CHAR, 
                         locX IN INTEGER, 
                         locY IN INTEGER, 
                         locZ IN INTEGER ) 
  AS 

-- Boxland - Bernard McManus 2012
-- Died_Event - Deals with the memory buffer of the dead mob

  eventID    INTEGER;
  decisionID INTEGER;
  locID      INTEGER;
  choiceID   INTEGER;
  killedID   INTEGER;
   
   -- The current events under consideration for this mob
   CURSOR currEvents is
   SELECT event_hist_id
     FROM event_hist_new
    WHERE obj_id = mobDBID
    ORDER BY event_hist_id;
  
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
    COMMIT;
    
    locID := event_loc_seq.currval;
    -- SELECT MAX(event_loc_id) INTO locID FROM EVENT_LOC;
  
  END IF;
  
  decisionID := return_decision_match ( mobDBID, choiceID, locID, -1 );
  
  IF decisionID is null 
  THEN

    INSERT INTO EVENT_DECISION ( EVENT_DECISION_ID, OBJ_ID, EVENT_CHOICE_ID, EVENT_LOC_ID, LAST_FACING ) 
      SELECT 0, 
             mobDBID, 
             choiceID,
             locID,
             -1 -- Is this right? Should it be a case?
        FROM DUAL;
    
    decisionID := event_decision_seq.currval;
    -- SELECT max(EVENT_DECISION_ID) INTO decisionID FROM EVENT_DECISION;

  END IF;
  
  killedID := return_last_action( mobDBID);

  INSERT INTO EVENT_HIST ( EVENT_HIST_ID, EVENT_DECISION_ID, EVENT_TYPE, CHOICE_FACING ) 
    SELECT 0, 
           decisionID, 
           way, 
           CASE way WHEN 'Killed' THEN -1
                    WHEN 'Starved' THEN -2
           END choice_facing
      FROM DUAL;
  
  -- Create a scoring record that this was bad
  SELECT MAX(event_hist_id) INTO eventID FROM event_hist;
  INSERT INTO event_scoring ( event_hist_id )
      SELECT eventID FROM DUAL;      
  
  -- They only learn that the last n < 49 moves leading to starvation were bad
  IF way = 'Starved' THEN
  
      -- Enter the top(chainCount) rows into the naughty table, except the 1st
    INSERT INTO EVENT_SCORING ( EVENT_HIST_ID )
      SELECT event_hist_id
        FROM event_hist_new 
       WHERE obj_id = mobDBID;
    
  ELSE
   
    -- If a mob is killed, remove the memories from the buffer and event_hist
    -- saved the 'killedID' event for the last thing leading to their deaths
    -- Being killed is pretty arbitrary but location info would be helpful
  
    IF killedID IS NOT NULL THEN
      INSERT INTO EVENT_SCORING ( EVENT_HIST_ID ) SELECT killedID EVENT_HIST_ID FROM DUAL;
    END IF;
     
    FOR delete_event_id in currEvents
    LOOP
      DELETE FROM event_hist_new where event_hist_id = delete_event_id.event_hist_id;
      DELETE FROM event_hist where event_hist_id = delete_event_id.event_hist_id
                               and delete_event_id.event_hist_id <> killedID;
    END LOOP;
  
  END IF;

  -- Clears out the new decisions for this jobject
  DELETE FROM event_hist_new WHERE obj_id = mobDBID;
  
END DIED_EVENT;