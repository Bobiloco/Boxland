create or replace 
FUNCTION RETURN_ECM_MATCH ( target0 IN CHAR,
                            target1 IN CHAR,
                            target2 IN CHAR,
                            target3 IN CHAR,
                            target4 IN CHAR,
                            target5 IN CHAR,
                            target6 IN CHAR )
  RETURN INTEGER 
AS 

choiceID INT;

BEGIN

  select event_choice_id
    into choiceID
    from event_choice_node ecn
   where ecn.ct0 = target0 and
         ecn.ct1 = target1 and
         ecn.ct2 = target2 and
         ecn.ct3 = target3 and
         ecn.ct4 = target4 and
         ecn.ct5 = target5 and
         ecn.ct6 = target6;

  RETURN choiceID;
  
  exception

  WHEN NO_DATA_FOUND
  
  THEN return NULL;
  
END RETURN_ECM_MATCH;