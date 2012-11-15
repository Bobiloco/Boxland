create or replace 
FUNCTION GET_BEST_FACING( mobDBID    IN INT, 
                          choiceID   IN INT, 
                          locID      IN INT,
                          lastFacing IN INT ) 
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
             WHEN 1 THEN NVL(eventType.pct,0) 
                       + NVL(eventLoc.pct,0)  
                       + NVL(choiceDir.pct,0)
                       + NVL(choiceFacing.pct,0)
                       + NVL(lastFace.pct,0) 
           END score
        from event_choice ec
      left join ( -- BY Last Move - should help correct faulty patterns
                   select eh.choice_facing, 
                        ( count(eh.event_hist_id) - count(ehn.event_hist_id) ) tot,
                        CASE WHEN count(eh.event_hist_id) - count(ehn.event_hist_id) = 0 THEN 0
                               ELSE ( ( count(eh.event_hist_id) - count(ehn.event_hist_id) ) - count(es.event_hist_id ) ) / ( count(eh.event_hist_id) - count(ehn.event_hist_id) )  
                          END pct
                     from event_hist eh
                     join event_decision ed on eh.event_decision_id = ed.event_decision_id
                                           and ed.last_facing = lastFacing
                     join obj o on ed.obj_id  = o.obj_id 
                               and o.obj_team = objTeam
                     left join event_scoring es on eh.event_hist_id = es.event_hist_id
                     left join event_hist_new ehn on eh.event_hist_id = ehn.event_hist_id
                    group by eh.choice_facing ) lastFace
        ON ec.choice_facing = lastFace.choice_facing
      left join ( -- BY Direction
                  select eh.choice_facing, 
                        ( count(eh.event_hist_id) - count(ehn.event_hist_id) ) tot,
                        CASE WHEN count(eh.event_hist_id) - count(ehn.event_hist_id) = 0 THEN 0 
                               ELSE ( ( count(eh.event_hist_id) - count(ehn.event_hist_id) ) - count(es.event_hist_id ) ) / ( count(eh.event_hist_id) - count(ehn.event_hist_id) )  
                          END pct
                     from event_hist eh
                     join event_decision ed on eh.event_decision_id = ed.event_decision_id
                     join obj o on ed.obj_id  = o.obj_id 
                               and o.obj_team = objTeam
                     left join event_scoring es on eh.event_hist_id = es.event_hist_id
                     left join event_hist_new ehn on eh.event_hist_id = ehn.event_hist_id
                    group by eh.choice_facing ) choiceDir
        ON ec.choice_facing = choiceDir.choice_facing
       left join ( -- BY EVENT - TYPE
                   select eh.event_type, 
                        ( count(eh.event_hist_id) - count(ehn.event_hist_id) ) tot,
                        CASE WHEN count(eh.event_hist_id) - count(ehn.event_hist_id) = 0 THEN 0 
                               ELSE ( ( count(eh.event_hist_id) - count(ehn.event_hist_id) ) - count(es.event_hist_id ) ) / ( count(eh.event_hist_id) - count(ehn.event_hist_id) )  
                          END pct
                     from event_hist eh
                     join event_decision ed on eh.event_decision_id = ed.event_decision_id
                     join obj o on ed.obj_id  = o.obj_id 
                               and o.obj_team = objTeam
                     left join event_scoring es on eh.event_hist_id = es.event_hist_id
                     left join event_hist_new ehn on eh.event_hist_id = ehn.event_hist_id
                    group by eh.event_type
                       ) eventType
        ON ec.choice_target = eventType.event_type
      left join ( -- BY CHOICE - FACING
                   select eh.choice_facing, 
                          ( count(eh.event_hist_id) - count(ehn.event_hist_id) ) tot,
                          CASE WHEN count(eh.event_hist_id) - count(ehn.event_hist_id) = 0 THEN 0 
                               ELSE ( ( count(eh.event_hist_id) - count(ehn.event_hist_id) ) - count(es.event_hist_id ) ) / ( count(eh.event_hist_id) - count(ehn.event_hist_id) )  
                          END pct
                     from event_hist eh
                     join event_decision ed on eh.event_decision_id = ed.event_decision_id
                                           AND ed.event_choice_id   = choiceID
                     join obj o on ed.obj_id = o.obj_id 
                               and o.obj_team = objTeam
                    left join event_scoring es on eh.event_hist_id = es.event_hist_id
                    left join event_hist_new ehn on eh.event_hist_id = ehn.event_hist_id
                    group by eh.choice_facing
                ) choiceFacing
        on ec.choice_facing   = choiceFacing.choice_facing
      left join ( -- By EVENT - LOC
                  select eh.choice_facing, 
                         ( count(eh.event_hist_id) - count(ehn.event_hist_id) ) tot,
                         CASE WHEN count(eh.event_hist_id) - count(ehn.event_hist_id) = 0 THEN 0 
                              ELSE ( ( count(eh.event_hist_id) - count(ehn.event_hist_id) ) - count(es.event_hist_id ) ) / ( count(eh.event_hist_id) - count(ehn.event_hist_id) )  
                         END pct
                   from event_hist eh
                   join event_decision ed on eh.event_decision_id = ed.event_decision_id
                                         and ed.event_loc_id = locID
                   join obj o on ed.obj_id = o.obj_id 
                             and o.obj_team = objTeam
                   left join event_scoring es on eh.event_hist_id = es.event_hist_id
                   left join event_hist_new ehn on eh.event_hist_id = ehn.event_hist_id
                  group by eh.choice_facing
                 ) eventLoc
         on ec.choice_facing = eventLoc.choice_facing
      where ec.event_choice_id = choiceID
     
  ) ORDER BY 2 DESC, dbms_random.value()

) WHERE ROWNUM = 1;

  RETURN retfacing;
  
  exception

  WHEN NO_DATA_FOUND
  
  THEN RETURN NULL;
  
END GET_BEST_FACING;