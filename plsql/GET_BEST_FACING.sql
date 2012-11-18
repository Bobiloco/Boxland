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
           CASE objCD 
             WHEN 0 THEN dbms_random.value()
             WHEN 1 THEN NVL(eventType.pct,0)
                       + CASE WHEN ( NVL(choiceDir.sumpos,0) <> 0 ) AND ( NVL(choiceDir.sumpct,0) <> 0 ) 
                              THEN ( NVL(choiceDir.pos,0) * NVL(choiceDir.pct,0) ) / ( NVL(choiceDir.sumpos,0) * NVL(choiceDir.sumpct,0) ) 
                                   * NVL(choiceDir.sumpos,0) / ( NVL(choiceDir.sumpos,0) + NVL(eventLoc.sumpos,0) + NVL(choiceFacing.sumpos,0) + NVL(lastFace.sumpos,0) )
                              ELSE 0 END
                       + CASE WHEN ( NVL(eventLoc.sumpos,0) <> 0 ) AND ( NVL(eventLoc.sumpct,0) <> 0 ) 
                              THEN ( NVL(eventLoc.pos,0) * NVL(eventLoc.pct,0) ) / ( NVL(eventLoc.sumpos,0) * NVL(eventLoc.sumpct,0) ) 
                                   * NVL(eventLoc.sumpos,0) / ( NVL(choiceDir.sumpos,0) + NVL(eventLoc.sumpos,0) + NVL(choiceFacing.sumpos,0) + NVL(lastFace.sumpos,0) )
                              ELSE 0 END
                       + CASE WHEN ( NVL(choiceFacing.sumpos,0) <> 0 ) AND ( NVL(choiceFacing.sumpct,0) <> 0 ) 
                              THEN ( NVL(choiceFacing.pos,0) * NVL(choiceFacing.pct,0) ) / ( NVL(choiceFacing.sumpos,0) * NVL(choiceFacing.sumpct,0) ) 
                                   * NVL(choiceFacing.sumpos,0) / ( NVL(choiceDir.sumpos,0) + NVL(eventLoc.sumpos,0) + NVL(choiceFacing.sumpos,0) + NVL(lastFace.sumpos,0) )
                              ELSE 0 END
                       + CASE WHEN ( NVL(lastFace.sumpos,0) <> 0 ) AND ( lastface.sumpct <> 0 ) 
                              THEN ( NVL(lastFace.pos,0) * NVL(lastFace.pct,0) ) / ( NVL(lastFace.sumpos,0) * NVL(lastFace.sumpct,0) )
                                   * NVL(lastFace.sumpos,0) / ( NVL(choiceDir.sumpos,0) + NVL(eventLoc.sumpos,0) + NVL(choiceFacing.sumpos,0) + NVL(lastFace.sumpos,0) )
                              ELSE 0 END
           WHEN 2 THEN 4 * NVL(eventType.pct,0) + NVL(choiceDir.pct,0) + NVL(eventLoc.pct,0) + NVL(choiceFacing.pct,0) + NVL(lastFace.pct,0)
           END score
        from event_choice ec
        left join ( -- BY EVENT - TYPE
                    select event_type, pos, pct, 
                           sum(pos) over (partition by ' ') sumpos, 
                           sum(pct) over (partition by ' ') sumpct
                     from                   
                  (  select eh.event_type, 
                            COUNT(eh.event_hist_id) - COUNT(ehn.event_hist_id) - COUNT(es.event_hist_id ) pos,
                            CASE WHEN COUNT(eh.event_hist_id) - COUNT(ehn.event_hist_id) = 0 THEN 0 
                                 ELSE ( ( COUNT(eh.event_hist_id) - COUNT(ehn.event_hist_id) ) - COUNT(es.event_hist_id ) ) / ( COUNT(eh.event_hist_id) - COUNT(ehn.event_hist_id) )  
                            END pct
                     from event_hist eh
                     join event_decision ed on eh.event_decision_id = ed.event_decision_id
                     join obj o on ed.obj_id  = o.obj_id 
                               and o.obj_team = objTeam
                     left join event_scoring es on eh.event_hist_id = es.event_hist_id
                     left join event_hist_new ehn on eh.event_hist_id = ehn.event_hist_id
                    where eh.event_type in ( select choice_target from event_choice where event_choice_id = choiceID group by choice_target ) -- for proper sums in the parent query
                    group by eh.event_type
                  ) eventT
                  ) eventType
        ON ec.choice_target = eventType.event_type
      left join ( -- BY Direction
                  select choice_facing, pos, pct, 
                         sum(pos) over (partition by ' ') sumpos, 
                         sum(pct) over (partition by ' ') sumpct
                  from                   
                ( select eh.choice_facing, 
                         count(eh.event_hist_id) - count(ehn.event_hist_id) - count(es.event_hist_id) pos,
                         CASE WHEN count(eh.event_hist_id) - count(ehn.event_hist_id) = 0 THEN 0 
                               ELSE ( count(eh.event_hist_id) - count(ehn.event_hist_id) - count(es.event_hist_id) ) / ( count(eh.event_hist_id) - count(ehn.event_hist_id) )  
                          END pct
                     from event_hist eh
                     join event_decision ed on eh.event_decision_id = ed.event_decision_id
                     join obj o on ed.obj_id  = o.obj_id 
                               and o.obj_team = objTeam
                     left join event_scoring es on eh.event_hist_id = es.event_hist_id
                     left join event_hist_new ehn on eh.event_hist_id = ehn.event_hist_id
                    where eh.choice_facing in ( select choice_facing from event_choice where event_choice_id = choiceID ) -- for proper sums in the parent query                     
                    group by eh.choice_facing 
                 ) choiceD
                 ) choiceDir
        ON ec.choice_facing = choiceDir.choice_facing
      left join ( -- BY Last Move - should help correct faulty patterns
                 select choice_facing, pos, pct, 
                        sum(pos) over (partition by ' ') sumpos, 
                        sum(pct) over (partition by ' ') sumpct
                  from                   
                (  select eh.choice_facing, 
                          COUNT(eh.event_hist_id) - COUNT(ehn.event_hist_id) - COUNT(es.event_hist_id ) pos,
                          CASE WHEN count(eh.event_hist_id) - count(ehn.event_hist_id) = 0 THEN 0
                               ELSE ( count(eh.event_hist_id) - count(ehn.event_hist_id) - count(es.event_hist_id ) ) / ( count(eh.event_hist_id) - count(ehn.event_hist_id) )  
                          END pct
                     from event_hist eh
                     join event_decision ed on eh.event_decision_id = ed.event_decision_id
                                           and ed.last_facing = lastFacing
                     join obj o on ed.obj_id  = o.obj_id 
                               and o.obj_team = objTeam
                     left join event_scoring es on eh.event_hist_id = es.event_hist_id
                     left join event_hist_new ehn on eh.event_hist_id = ehn.event_hist_id
                    where eh.choice_facing in ( select choice_facing from event_choice where event_choice_id = choiceID ) -- for proper sums in the parent query
                    group by eh.choice_facing
                ) lastF
                ) lastFace
         ON ec.choice_facing = lastFace.choice_facing
       left join ( -- BY CHOICE - FACING
                   select choice_facing, pos, pct, 
                        sum(pos) over (partition by ' ') sumpos, 
                        sum(pct) over (partition by ' ') sumpct
                  from                   
                (  select eh.choice_facing, 
                          COUNT(eh.event_hist_id) - COUNT(ehn.event_hist_id) - COUNT(es.event_hist_id ) pos,
                          CASE WHEN count(eh.event_hist_id) - count(ehn.event_hist_id) = 0 THEN 0 
                               ELSE ( ( count(eh.event_hist_id) - count(ehn.event_hist_id) ) - count(es.event_hist_id ) ) / ( count(eh.event_hist_id) - count(ehn.event_hist_id) )  
                          END pct
                     FROM event_hist eh
                     JOIN event_decision ed ON eh.event_decision_id = ed.event_decision_id
                                           AND ed.event_choice_id   = choiceID  -- this should take care of the sums in the parent query
                     JOIN obj o on ed.obj_id = o.obj_id 
                               and o.obj_team = objTeam
                    left JOIN event_scoring es on eh.event_hist_id = es.event_hist_id
                    left JOIN event_hist_new ehn on eh.event_hist_id = ehn.event_hist_id
                    GROUP by eh.choice_facing
                ) choiceF
                ) choiceFacing
        on ec.choice_facing   = choiceFacing.choice_facing
      left join ( -- By EVENT - LOC
                   select choice_facing, pos, pct, 
                        sum(pos) over (partition by ' ') sumpos, 
                        sum(pct) over (partition by ' ') sumpct
                  from                   
                (  select eh.choice_facing, 
                         COUNT(eh.event_hist_id) - COUNT(ehn.event_hist_id) - COUNT(es.event_hist_id ) pos,
                         CASE WHEN COUNT(eh.event_hist_id) - COUNT(ehn.event_hist_id) = 0 THEN 0 
                              ELSE ( ( count(eh.event_hist_id) - COUNT(ehn.event_hist_id) ) - COUNT(es.event_hist_id ) ) / ( COUNT(eh.event_hist_id) - COUNT(ehn.event_hist_id) )  
                         END pct
                   from event_hist eh
                   join event_decision ed on eh.event_decision_id = ed.event_decision_id
                                         and ed.event_loc_id = locID  -- this should take care of the sums in the parent query
                   join obj o on ed.obj_id = o.obj_id 
                             and o.obj_team = objTeam
                   left join event_scoring es on eh.event_hist_id = es.event_hist_id
                   left join event_hist_new ehn on eh.event_hist_id = ehn.event_hist_id
                  group by eh.choice_facing
                 ) eventL
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