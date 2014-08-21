create or replace 
FUNCTION GET_BEST_TARGET_FACING( mobDBID IN INTEGER, 
                                choiceID IN INTEGER ) 
RETURN INTEGER 
AS 

-- Boxland - Bernard McManus 2012
-- Get_Best_Target_Facing - compares the events at a choice and returns the best one ( by facing ), 
                          -- or null if 'Vacant'

retFacing INTEGER;
objTeam   CHAR(10);

BEGIN

   SELECT obj_team
     INTO objTeam
     FROM obj
    WHERE obj_id = mobDBID;

   -- Check if an event type is an obvious choice, then chose it 
   -- ( or a random choice for that event type )
    select choice_facing
      into retFacing
      from (
    
    select choice_facing, pct from (
    
    select choice_facing, NVL(choiceType.pct,0) pct
      from event_choice ec
      LEFT JOIN (
           select eh.event_type, 
                  CASE WHEN COUNT(eh.event_hist_id) - COUNT(ehn.event_hist_id) = 0 THEN 0 
                       ELSE ( ( COUNT(eh.event_hist_id) - COUNT(ehn.event_hist_id) ) - COUNT(es.event_hist_id ) ) / ( COUNT(eh.event_hist_id) - COUNT(ehn.event_hist_id) )  
                  END pct
           from event_hist eh
           join event_decision ed on eh.event_decision_id = ed.event_decision_id
           join obj o on ed.obj_id  = o.obj_id 
                     and o.obj_team = objTeam
           left join event_scoring es on eh.event_hist_id = es.event_hist_id
           left join event_hist_new ehn on eh.event_hist_id = ehn.event_hist_id
          where eh.event_type in ( select choice_target from event_choice where event_choice_id = choiceID group by choice_target )
          group by eh.event_type
        ) choiceType
       on ec.choice_target  = choiceType.event_type

        ) choiceT order by 2 desc, dbms_random.value()
        
        ) where rownum = 1;
        
    RETURN retFacing;
        
END GET_BEST_TARGET_FACING;