create or replace 
FUNCTION RETURN_FIRST_MOVE ( mobDBID IN INT )
RETURN INT 
AS 

  firstMoveID INT;

BEGIN
  
  SELECT min(eh.event_hist_id) 
    INTO firstMoveID
    FROM event_hist eh
    JOIN event_decision ed ON eh.event_decision_id = ed.event_decision_id
   WHERE ed.obj_id = mobDBID;
    
  RETURN firstMoveID;
  
  exception

  WHEN NO_DATA_FOUND
  
  THEN RETURN NULL;
  
END RETURN_FIRST_MOVE;