create or replace 
FUNCTION RETURN_LAST_START ( mobDBID IN INT )
RETURN INT 
AS 

  lastStartID INT;

BEGIN
  
  lastStartID := return_last_killed( mobDBID );
  
  IF lastStartID IS NULL 
    THEN lastStartID := return_first_move( mobDBID );
  END IF;
  
  return lastStartID;
  
  EXCEPTION

  WHEN NO_DATA_FOUND
  
  THEN return NULL;
  
END RETURN_LAST_START;