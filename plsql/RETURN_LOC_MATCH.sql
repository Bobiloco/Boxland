CREATE OR REPLACE
FUNCTION RETURN_LOC_MATCH ( locX IN INTEGER, 
                            locY IN INTEGER, 
                            locZ IN INTEGER )
RETURN INTEGER
AS 

locID INTEGER;

BEGIN

  select event_loc_id
    into locID
    from event_loc
   where loc_X = locX
     and loc_Y = locY 
     and loc_Z = locZ;

  RETURN locID;
  
EXCEPTION
  WHEN NO_DATA_FOUND
  THEN return NULL;
END RETURN_LOC_MATCH;