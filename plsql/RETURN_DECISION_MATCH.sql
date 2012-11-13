create or replace 
FUNCTION RETURN_DECISION_MATCH
( mobDBID IN INT, choiceID in INT, locID IN INT, lastFacing IN INT )
  RETURN INT 
AS 

decisionID INT;

BEGIN

  select ed.event_decision_id
    into decisionID
    from event_decision ed
   where ed.obj_id = mobDBID 
     AND ed.event_choice_id = choiceID
     AND ed.event_loc_id = locID
     AND ed.last_facing = lastFacing;

  RETURN decisionID;
  
EXCEPTION
  WHEN NO_DATA_FOUND
  THEN return NULL;
END RETURN_DECISION_MATCH;