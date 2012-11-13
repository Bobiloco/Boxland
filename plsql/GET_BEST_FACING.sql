create or replace 
FUNCTION GET_BEST_FACING( mobDBID    IN INT, 
                          choiceID   IN INT, 
                          locID      IN INT,
                          lastFacing IN INT,
                          weightCD   IN FLOAT,
                          weightET   IN FLOAT,
                          weightCF   IN FLOAT,
                          weightEL   IN FLOAT,
                          weightLF   IN FLOAT ) 
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
  SELECT choice_facing, score 
    FROM (
  
    -- contains the formula for weighing the join scores
    select ec.choice_facing, 
           --NVL(eventType.score,0) eventType, 
           --NVL(choiceFacing.score,0) choiceFacing,
           --NVL(eventLoc.score,0) eventLoc,
           --NVL(eventLoc.score,0) choiceDir,
           CASE objCD 
             WHEN 0 THEN dbms_random.value()
             WHEN 1 THEN ( weightET * NVL(eventType.pct,0)) 
                       + ( weightEL * NVL(eventLoc.pct,0))  
                       + ( weightCD * NVL(choiceDir.pct,0)) 
                       + ( weightCF * NVL(choiceFacing.pct,0))
                       + ( weightLF * NVL(lastFace.pct,0))
             WHEN 2 THEN CASE NVL(eventType.tot,0) WHEN 0 THEN weightET ELSE ( weightET * NVL(eventType.pct,0)) END
                       + CASE NVL(eventLoc.tot,0) WHEN 0 THEN weightEL ELSE ( weightEL * NVL(eventLoc.pct,0)) END
                       + CASE NVL(choiceDir.tot,0) WHEN 0 THEN weightCD ELSE ( weightCD * NVL(choiceDir.pct,0)) END
                       + CASE NVL(choiceFacing.tot,0) WHEN 0 THEN weightCF ELSE ( weightCF * NVL(choiceFacing.pct,0)) END
                       + CASE NVL(lastFace.tot,0) WHEN 0 THEN weightLF ELSE ( weightLF * NVL(lastFace.pct,0)) END  
             WHEN 3 THEN CASE NVL(eventType.tot,0) WHEN 0 THEN 1 ELSE ( weightET * NVL(eventType.pct,0)) END
                       + CASE NVL(eventLoc.tot,0) WHEN 0 THEN 1 ELSE ( weightEL * NVL(eventLoc.pct,0)) END
                       + CASE NVL(choiceDir.tot,0) WHEN 0 THEN 1 ELSE ( weightCD * NVL(choiceDir.pct,0)) END
                       + CASE NVL(choiceFacing.tot,0) WHEN 0 THEN 1 ELSE ( weightCF * NVL(choiceFacing.pct,0)) END
                       + CASE NVL(lastFace.tot,0) WHEN 0 THEN 1 ELSE ( weightLF * NVL(lastFace.pct,0)) END                         
           END score
        from event_choice ec
      left join ( -- BY Last Move - should help correct faulty patterns
                  -- The percentage of the time a team do... /see an action that has turned out badly
                select score1.choice_facing,  
                       score2.score tot,
                       ( score2.score - score1.score ) / score2.score pct
                  FROM (
                    select eh.choice_facing, ed.last_facing, count(*) score
                      from event_hist eh
                      join event_decision ed on eh.event_decision_id = ed.event_decision_id
                      join event_scoring es on eh.event_hist_id = es.event_hist_id
                     where ed.last_facing = lastFacing
                     group by eh.choice_facing, ed.last_facing ) score1
                  join (
                     select eh.choice_facing, ed.last_facing, count(*) score
                      from event_hist eh
                      join event_decision ed on eh.event_decision_id = ed.event_decision_id
                      left join event_hist_new ehn on eh.event_hist_id = ehn.event_hist_id
                     where ehn.event_hist_id is null
                       and ed.last_facing = lastFacing
                     group by eh.choice_facing, ed.last_facing ) score2
                    on score1.choice_facing = score2.choice_facing ) lastFace
        ON ec.choice_facing = lastFace.choice_facing
      left join ( -- BY Direction
                  -- The percentage of the time a team do... /see an action that has turned out badly
                  select score1.choice_facing,  
                         score2.score tot,  
                       ( score2.score - score1.score ) / score2.score pct
                       from ( -- How many times they scored the action as bad, across the team
                           select eh.choice_facing, count(*) score
                             from event_hist eh
                             join event_scoring es on eh.event_hist_id = es.event_hist_id
                             join event_decision ed on eh.event_decision_id = ed.event_decision_id
                             join obj o on ed.obj_id  = o.obj_id 
                                       and o.obj_team = objTeam
                            group by eh.choice_facing ) score1
                     join ( -- how many times they chose the action
                            select eh.choice_facing, count(*) score
                             from event_hist eh
                             join event_decision ed on eh.event_decision_id = ed.event_decision_id
                             join obj o on ed.obj_id  = o.obj_id 
                                       and o.obj_team = objTeam
                             left join event_hist_new ehn on eh.event_hist_id = ehn.event_hist_id
                            where ehn.event_hist_id is null
                       group by eh.choice_facing ) score2
                     ON score1.choice_facing = score2.choice_facing ) choiceDir
        ON ec.choice_facing = choiceDir.choice_facing
      left join ( -- BY EVENT - TYPE
                  -- The percentage of the time a team do... /see an action that has turned out badly
                select score1.event_type,  
                       score2.score tot,  
                     ( score2.score - score1.score ) / score2.score pct
                    from ( -- How many times they scored the action as bad, across the team
                           select eh.event_type, count(*) score
                             from event_hist eh
                             join event_scoring es on eh.event_hist_id = es.event_hist_id
                             join event_decision ed on eh.event_decision_id = ed.event_decision_id
                             join obj o on ed.obj_id  = o.obj_id 
                                       and o.obj_team = objTeam
                            group by eh.event_type ) score1
                     join ( -- how many times they chose the action
                            select eh.event_type, count(*) score
                              from event_hist eh
                              join event_decision ed on eh.event_decision_id = ed.event_decision_id
                              join obj o on ed.obj_id  = o.obj_id 
                                        and o.obj_team = objTeam
                              left join event_hist_new ehn on eh.event_hist_id = ehn.event_hist_id
                             where ehn.event_hist_id is null
                             group by event_type ) score2
                     ON score1.event_type = score2.event_type ) eventType
        ON ec.choice_target = eventType.event_type
      left join ( -- BY CHOICE - FACING
                  -- By choice (node), what facing has bad memories by percentages
                  select score1.choice_facing,  
                         score2.score tot,
                       ( score2.score - score1.score ) / score2.score pct
                    from ( -- Individual choice counts
                           select o.obj_team, ed.event_choice_id, eh.choice_facing, count(*) score
                             from event_hist eh
                             join event_scoring es on eh.event_hist_id = es.event_hist_id
                             join event_decision ed on eh.event_decision_id = ed.event_decision_id
                                                   AND ed.event_choice_id   = choiceID
                             join obj o on ed.obj_id = o.obj_id 
                                       and o.obj_team = objTeam
                            group by o.obj_team, ed.event_choice_id, eh.choice_facing ) score1
                    JOIN ( -- How many times chosen
                           select o.obj_team, ed.event_choice_id, eh.choice_facing, count(*) score
                             from event_hist eh
                             join event_decision ed on eh.event_decision_id = ed.event_decision_id
                                                   AND ed.event_choice_id   = choiceID
                             join obj o on ed.obj_id  = o.obj_id 
                                       and o.obj_team = objTeam
                        left join event_hist_new ehn on eh.event_hist_id = ehn.event_hist_id
                            where ehn.event_hist_id is null
                             group by o.obj_team, ed.event_choice_id, eh.choice_facing ) score2
                     ON score1.choice_facing = score2.choice_facing ) choiceFacing
        on ec.choice_facing   = choiceFacing.choice_facing
      left join ( -- By EVENT - LOC
                  -- For this locID, how badly did chosing a given facing turn out, by facing
                  --   By percentage: ( scored / chosen )
                  SELECT score1.choice_facing,  
                         score2.score tot,
                       ( score2.score - score1.score ) / score2.score pct
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
                             left join event_hist_new ehn on eh.event_hist_id = ehn.event_hist_id
                            where el.event_loc_id = locID
                              and ehn.event_hist_id is null
                            group by el.event_loc_id, eh.choice_facing
                          ) score2
                      ON score1.event_loc_id = score2.event_loc_id
                     AND score1.choice_facing   = score2.choice_facing ) eventLoc
         on ec.choice_facing = eventLoc.choice_facing
      where ec.event_choice_id = choiceID
     
  ) ORDER BY 2 DESC, dbms_random.value()

) WHERE ROWNUM = 1;

  RETURN retfacing;
  
  exception

  WHEN NO_DATA_FOUND
  
  THEN RETURN NULL;
  
END GET_BEST_FACING;