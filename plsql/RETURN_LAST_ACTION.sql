create or replace 
FUNCTION RETURN_LAST_ACTION ( mobDBID IN INT )
RETURN INT 
AS 

killedID INT;

BEGIN
  
   -- Get last action by this mob
    SELECT MAX(event_hist_id)
    INTO killedID
    FROM event_hist_new
   WHERE obj_id = mobDBID;
  
  RETURN killedID;
  
  EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
  
END RETURN_LAST_ACTION;