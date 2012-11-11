create or replace 
FUNCTION GET_BEST_FACING( mobDBID IN INT, choiceID IN INT, locID IN INT ) 
RETURN INT 
AS 

retFacing INT;
objCD     INT;
objTeam   VARCHAR2(10);
choiceCap FLOAT := 0.9;

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
  SELECT choice_facing, score 
    FROM (
  
    -- contains the formula for weighing the join scores
    select ec.choice_facing, 
           --NVL(eventType.score,0) eventType, 
           --NVL(choiceFacing.score,0) choiceFacing,
           --NVL(eventLoc.score,0) eventLoc,
           CASE objCD 
             WHEN 0 THEN dbms_random.value()
             WHEN 1 THEN NVL(eventType.score,0)
             WHEN 2 THEN NVL(choiceFacing.score,0)
             WHEN 3 THEN NVL(eventLoc.score,0)
             WHEN 4 THEN ( 10* NVL(eventType.score,0) ) + NVL(choiceFacing.score,0) + NVL(eventLoc.score,0)
             WHEN 5 THEN NVL(eventType.score,0) + NVL(choiceFacing.score,0) + NVL(eventLoc.score,0)
           END score
      from event_choice ec
      left join ( -- BY CHOICE - FACING
                  -- By choice (node), what facing has bad memories by percentages
                  select score1.choice_facing, 
                    CASE WHEN score1.score / score2.score > choiceCap 
                              THEN choiceCap
                              ELSE score1.score / score2.score
                          END score
                    from ( -- Individual choice counts
                           select o.obj_team, ed.event_choice_id, eh.choice_facing, count(*) score
                             from event_hist eh
                             join event_scoring es on eh.event_hist_id = es.event_hist_id
                             join event_decision ed on eh.event_decision_id = ed.event_decision_id
                                                   AND ed.event_choice_id = choiceID
                             join obj o on ed.obj_id = o.obj_id 
                                       and o.obj_team = objTeam
                            group by o.obj_team, ed.event_choice_id, eh.choice_facing ) score1
                    JOIN ( -- How many times chosen
                           select o.obj_team, ed.event_choice_id, eh.choice_facing, count(*) score
                             from event_hist eh
                             join event_decision ed on eh.event_decision_id = ed.event_decision_id
                                                   AND ed.event_choice_id = choiceID
                             join obj o on ed.obj_id = o.obj_id 
                                       and o.obj_team = objTeam
                            where eh.event_hist_id not in ( select ehs.event_hist_id
                                                              from event_hist_new ehs
                                                             where eh.event_hist_id = ehs.event_hist_id)
                            group by o.obj_team, ed.event_choice_id, eh.choice_facing ) score2
                     ON score1.choice_facing = score2.choice_facing ) choiceFacing
        on ec.choice_facing   = choiceFacing.choice_facing
      left join ( -- BY EVENT - TYPE
                  -- The percentage of the time a team do... /see an action that has turned out badly
                select score1.event_type, 
                   CASE WHEN score1.score / score2.score > choiceCap 
                              THEN choiceCap
                              ELSE score1.score / score2.score
                          END score
                    from ( -- How many times they scored the action as bad, across the team
                           select eh.event_type, count(*) score
                             from event_hist eh
                             join event_scoring es on eh.event_hist_id = es.event_hist_id
                             join event_decision ed on eh.event_decision_id = ed.event_decision_id
                             join obj o on ed.obj_id = o.obj_id 
                                       and o.obj_team = objTeam
                            group by eh.event_type ) score1
                     join ( -- how many times they chose the action
                            select eh.event_type, count(*) score
                              from event_hist eh
                              join event_decision ed on eh.event_decision_id = ed.event_decision_id
                              join obj o on ed.obj_id = o.obj_id 
                                        and o.obj_team = objTeam
                             where eh.event_hist_id not in ( select ehs.event_hist_id
                                                               from event_hist_new ehs
                                                              where eh.event_hist_id = ehs.event_hist_id)
                            group by event_type ) score2
                     ON score1.event_type = score2.event_type ) eventType
        ON ec.choice_target = eventType.event_type
      left join ( -- By EVENT - LOC
                  -- For this locID, how badly did chosing a given facing turn out, by facing
                  --   By percentage: ( scored / chosen )
                  SELECT score1.choice_facing, 
                    CASE WHEN score1.score / score2.score > choiceCap 
                              THEN choiceCap
                              ELSE score1.score / score2.score
                          END score
                    FROM ( -- How many times they regretted a direction
                           select el.event_loc_id, eh.choice_facing,  count(*) score
                             from event_loc el
                             join event_decision ed on el.event_loc_id = ed.event_loc_id
                             join obj o on ed.obj_id = o.obj_id 
                                       and o.obj_team = objTeam
                             join event_hist eh on ed.event_decision_id = eh.event_decision_id
                             join event_scoring es on eh.event_hist_id = es.event_hist_id
                            where el.event_loc_id = locID
                            group by el.event_loc_id, eh.choice_facing
                          ) score1
                    JOIN ( -- how many times they went that way
                           select el.event_loc_id, eh.choice_facing,  count(*) score
                             from event_loc el
                             join event_decision ed on el.event_loc_id = ed.event_loc_id
                             join obj o on ed.obj_id = o.obj_id 
                                       and o.obj_team = objTeam
                             join event_hist eh on ed.event_decision_id = eh.event_decision_id
                            where el.event_loc_id = locID
                              AND eh.event_hist_id not in ( select ehs.event_hist_id
                                                              from event_hist_new ehs
                                                             where eh.event_hist_id = ehs.event_hist_id)
                            group by el.event_loc_id, eh.choice_facing
                          ) score2
                      ON score1.event_loc_id = score2.event_loc_id
                     AND score1.choice_facing   = score2.choice_facing ) eventLoc
         on ec.choice_facing = eventLoc.choice_facing
      where ec.event_choice_id = choiceID
     
  ) ORDER BY 2, dbms_random.value()

) WHERE ROWNUM = 1;

  RETURN retfacing;
  
  exception

  WHEN NO_DATA_FOUND
  
  THEN RETURN NULL;
  
END GET_BEST_FACING;