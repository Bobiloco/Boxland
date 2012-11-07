create or replace 
FUNCTION GET_BEST_CHOICE( mobTeam IN VARCHAR2, choiceID IN INT ) 
RETURN INT 
AS 

retFacing INT;

BEGIN

   SELECT choice_facing
    INTO retFacing 
    FROM (
    
-- returning void here at the beginning

SELECT choice_facing, scorfe FROM (

select ed.event_choice_id, eh.choice_facing, o.obj_team, count(*) score
  from event_hist eh
  join event_scoring es on eh.event_hist_id = es.event_hist_id
  join event_decision ed on eh.event_decision_id = ed.event_decision_id
  join obj o on ed.obj_id = o.obj_id and o.obj_team = mobTeam
  join event_choice ec on ed.event_choice_id = ec.event_choice_id
 WHERE ed.event_choice_id = choiceID
 group by ed.event_choice_id, eh.choice_facing, o.obj_team
 
 ) ORDER BY 2

) WHERE ROWNUM = 1;

  RETURN retfacing;
  
  exception

  WHEN NO_DATA_FOUND
  
  THEN RETURN NULL;
  
END GET_BEST_CHOICE;