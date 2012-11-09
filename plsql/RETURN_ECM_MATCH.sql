create or replace 
FUNCTION RETURN_ECM_MATCH ( target0 IN VARCHAR,
                                  target1 IN VARCHAR,
                                  target2 IN VARCHAR,
                                  target3 IN VARCHAR,
                                  target4 IN VARCHAR,
                                  target5 IN VARCHAR,
                                  target6 IN VARCHAR )
  RETURN INT 
AS 

choiceID INT;

BEGIN

  select event_choice_id
    into choiceID
    from event_choice_node ecn
   where ecn.ct0 = nvl(target0,' ') and
         ecn.ct1 = nvl(target1,' ') and
         ecn.ct2 = nvl(target2,' ') and
         ecn.ct3 = nvl(target3,' ') and
         ecn.ct4 = nvl(target4,' ') and
         ecn.ct5 = nvl(target5,' ') and
         ecn.ct6 = nvl(target6,' ');

  RETURN choiceID;
  
  exception

  WHEN NO_DATA_FOUND
  
  THEN return NULL;
  
END RETURN_ECM_MATCH;