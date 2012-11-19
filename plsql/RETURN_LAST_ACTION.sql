create or replace 
FUNCTION RETURN_LAST_ACTION ( mobDBID IN INTEGER )
RETURN INTEGER 
AS 

-- Boxland - Bernard McManus 2012
-- Return_last_action.sql - Creates rows for a decision in the database

killedID INTEGER;

BEGIN
  
   -- Get last action by this mob
    SELECT MAX(event_hist_id)
    INTO killedID
    FROM event_hist_new
   WHERE obj_id = mobDBID;
  
  RETURN killedID;
  
  EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
  
END RETURN_LAST_ACTION;