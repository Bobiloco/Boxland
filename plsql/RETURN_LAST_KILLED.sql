create or replace 
FUNCTION RETURN_LAST_KILLED ( mobDBID IN INT )
RETURN INT 
AS 

  lastKilledID INT;

BEGIN
  
  SELECT max(event_hist_id)
    INTO lastKilledID
    FROM event_hist eh
    JOIN event_decision ed on eh.event_decision_id = ed.event_decision_id
   WHERE ed.obj_id = mobDBID
     AND eh.event_type = 'Killed';
  
  return lastKilledId;
  
  exception

  WHEN NO_DATA_FOUND
  
  THEN return NULL;
  
END RETURN_LAST_KILLED;