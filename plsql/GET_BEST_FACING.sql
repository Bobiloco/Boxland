create or replace 
FUNCTION GET_BEST_FACING( mobDBID IN INT, choiceID IN INT ) 
RETURN INT 
AS 

retFacing INT;
objCD     INT;
objTeam   VARCHAR2(10);

BEGIN

   SELECT obj_team
     INTO objTeam
     FROM obj
    WHERE obj_id = mobDBID;

   SELECT obj_cd
     INTO objCD
     FROM obj
    WHERE obj_id = mobDBID;

-- This is the outer select for the best facing choice, rownum = 1 with ties randomized
SELECT choice_facing
  INTO retFacing 
  FROM (
    
  -- returning the final scores for all the facings
  SELECT choice_facing, score FROM (
  
    -- contains the formula for weighing the join scores
    select ec.choice_facing, 
           CASE objCD 
             WHEN 0 THEN dbms_random.value()
             WHEN 1 THEN NVL(targetScoring.score,0)
             WHEN 2 THEN NVL(stateScoring.score,0)
             WHEN 3 THEN NVL(targetScoring.score,0) + NVL(stateScoring.score,0)
           END score
      from event_choice ec
      left join ( -- By choice, what facing has bad memories and how many
                  select o.obj_team, ed.event_choice_id, eh.choice_facing, count(*) score
                    from event_hist eh
                    join event_scoring es on eh.event_hist_id = es.event_hist_id
                    join event_decision ed on eh.event_decision_id = ed.event_decision_id
                    join obj o on ed.obj_id = o.obj_id and o.obj_team = objTeam
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
                            join obj o on ed.obj_id = o.obj_id and o.obj_team = objTeam
                           group by event_type, obj_team 
                         ) score1
                   join ( -- How many times they scored the action, across the team
                          select ec.choice_target event_type, o.obj_team obj_team, count(*) score
                            from event_hist eh
                            join event_scoring es on eh.event_hist_id = es.event_hist_id
                            join event_decision ed on eh.event_decision_id = ed.event_decision_id
                            join obj o on ed.obj_id = o.obj_id and o.obj_team = objTeam
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
  
END GET_BEST_FACING;