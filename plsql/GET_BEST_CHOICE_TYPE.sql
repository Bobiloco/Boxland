create or replace 
FUNCTION GET_BEST_CHOICE_TYPE( mobDBID IN INTEGER, 
                              choiceID IN INTEGER ) 
RETURN INTEGER 
AS 

-- Boxland - Bernard McManus 2012
-- Get_Best_choice_type - decides on a move based on better choices than move/stay

retFacing INTEGER;
retTarget CHAR(10);
objTeam   CHAR(10);


BEGIN

   SELECT obj_team
     INTO objTeam
     FROM obj
    WHERE obj_id = mobDBID;

   -- Check if an event is an obvious choice

    select choice_facing
      into retFacing
      from (
    
    select choice_facing, pct from (
    
    select choice_facing, 
           CASE 
             WHEN tot = 0  -- if there are no event_hist rows that have tried the choice
               THEN 1     --  ( even in the ehn buffer ) try it out 100% of the time. Something new!
             ELSE 
               NVL(choiceTYPE.pct,0) -- whatever the percent failure is
           END pct
      from event_choice ec
      LEFT JOIN (
           select eh.event_type, 
                  COUNT(eh.event_hist_id) tot,
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

        ) choiceT order by 2 desc
        
        ) where rownum = 1;
        
    -- Check if it was 'Vacant', and if so return null so that a best move can be chosen
    SELECT choice_target 
      into retTarget
      From event_choice 
     where choice_facing = retFacing;
     
    -- If it wasn't an attack/eat/bump move, they should use get_best_move, so return null
    IF retTarget in ( 'Vacant', 'Self' )
      THEN RETURN null; 
    ELSE RETURN retFacing;
    END IF;
        
END GET_BEST_CHOICE_TYPE;