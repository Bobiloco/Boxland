create or replace 
PROCEDURE KILLED_EVENT( mobDBID IN INT, locX IN INT, locY in INT, locZ IN INT ) 
  AS 

  eventID INT;
  decisionID INT;
  locID INT;
  choiceID INT;
  lastStartID INT;
  chainCount INT;
 
  BEGIN

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

  -- grab the last start, potientially a 'Killed', before recording the current event
  lastStartID := return_last_start( mobDBID );
 
  INSERT INTO EVENT_HIST ( EVENT_HIST_ID, EVENT_DECISION_ID, EVENT_TYPE, CHOICE_FACING ) 
    SELECT 0, decisionID, 'Killed', 0 FROM DUAL;

  SELECT max(EVENT_HIST_ID) 
    INTO eventID
    FROM EVENT_HIST;

   -- Could return null if they were killed first turn, in which case we're done scoring
   IF lastStartID IS NULL
     THEN RETURN;
   END IF;
   
   -- So now we have the current event and the start of the chain of events

   -- Figure if the chain is > 100 and cut it down if it is
   SELECT COUNT(*)
     INTO chainCount
     FROM event_hist eh
     JOIN event_decision ed ON eh.event_decision_id = ed.event_decision_id
    WHERE ed.obj_id = mobDBID AND
          eh.event_hist_id >= lastStartID;
    
    IF chainCount > 50 THEN select 50 into chainCount from dual; END IF;
      
    -- Enter the top(chainCount) rows into the naughty table, except the 1st
    INSERT INTO EVENT_SCORING ( EVENT_HIST_ID )
    SELECT eh.event_hist_id
      FROM (
        SELECT eh.event_hist_id
          FROM event_hist eh
          JOIN event_decision ed ON eh.event_decision_id = ed.event_decision_id
                                AND ed.obj_id = mobDBID
         ORDER BY 1 DESC
        ) eh
     WHERE ROWNUM < chainCount;
     
END KILLED_EVENT;