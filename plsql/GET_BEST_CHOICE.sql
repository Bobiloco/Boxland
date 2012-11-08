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

SELECT choice_facing, score FROM (

select ec.choice_facing, 
       NVL( ( stateScoring.score * 10 ),0) + 10 * NVL(targetScoring.score,0) score
  from event_choice ec
  left join ( -- By choice, what facing has bad memories and how many
              select o.obj_team, ed.event_choice_id, eh.choice_facing, count(*) score
                from event_hist eh
                join event_scoring es on eh.event_hist_id = es.event_hist_id
                join event_decision ed on eh.event_decision_id = ed.event_decision_id
                join obj o on ed.obj_id = o.obj_id and o.obj_team = mobTeam
               WHERE ed.event_choice_id = choiceID
               group by o.obj_team, ed.event_choice_id, eh.choice_facing ) stateScoring
    on ec.event_choice_id = stateScoring.event_choice_id and
       ec.choice_facing   = stateScoring.choice_facing
  left join ( -- By team, the percentage of the time they do/see an action that has turned out badly
             select score1.obj_team, score1.event_type, (score1.score / score2.score) score
               from ( -- how many times they chose the action
                      select eh.event_type, o.obj_team, count(*) score
                        from event_hist eh
                        join event_scoring es on eh.event_hist_id = es.event_hist_id
                        join event_decision ed on eh.event_decision_id = ed.event_decision_id
                        join obj o on ed.obj_id = o.obj_id and o.obj_team = mobTeam
                       group by event_type, obj_team 
                     ) score1
               join ( -- How many times they scored the action, across the team
                      select ec.choice_target event_type, o.obj_team obj_team, count(*) score
                        from event_hist eh
                        join event_scoring es on eh.event_hist_id = es.event_hist_id
                        join event_decision ed on eh.event_decision_id = ed.event_decision_id
                        join obj o on ed.obj_id = o.obj_id and o.obj_team = mobTeam
                        join event_choice ec on ed.event_choice_id = ec.event_choice_id
                       group by ec.choice_target, o.obj_team 
                     ) score2
                 ON score1.event_type = score2.event_type 
                AND score1.obj_team   = score2.obj_team ) targetScoring
    on ec.choice_target = targetScoring.event_type
 where ec.event_choice_id = choiceID
  
 ) ORDER BY 2, dbms_random.value()

) WHERE ROWNUM = 1;

  RETURN retfacing;
  
  exception

  WHEN NO_DATA_FOUND
  
  THEN RETURN NULL;
  
END GET_BEST_CHOICE;