create or replace 
FUNCTION RETURN_LAST_FACING ( mobDBID IN INT )
RETURN INT 
AS 

lastFacing INT;

BEGIN
  
   -- Get last action by this mob
  SELECT choice_facing
    INTO lastFacing
    FROM event_hist
   WHERE event_hist_id = (
    SELECT MAX(event_hist_id) event_hist_id
      FROM event_hist eh
      JOIN event_decision ed on eh.event_decision_id = ed.event_decision_id
     WHERE ed.obj_id = mobDBID );
  
  RETURN lastFacing;
  
  EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
  
END RETURN_LAST_FACING;