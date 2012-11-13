create or replace 
PROCEDURE INIT_TABLES AS 

 choiceID INT;

BEGIN
  
  DELETE FROM EVENT_HIST_NEW;
  DELETE FROM EVENT_SCORING;
  DELETE FROM EVENT_HIST;
	DELETE FROM EVENT_DECISION;
  DELETE FROM EVENT_FACING_LAST;
  DELETE FROM EVENT_LOC;
	DELETE FROM EVENT_CHOICE;
  DELETE FROM EVENT_CHOICE_NODE;
	DELETE FROM OBJ;

  -- Create the 'Killed' dummy choice row
  INSERT INTO event_choice_node ( event_choice_id, ct0, ct1, ct2, ct3, ct4, ct5, ct6 )
    SELECT 0, ' ', ' ', ' ', ' ', ' ', ' ', ' ' FROM DUAL;
    
  INSERT INTO event_facing_last ( last_facing ) select 0 from dual;
  INSERT INTO event_facing_last ( last_facing ) select 1 from dual;
  INSERT INTO event_facing_last ( last_facing ) select 2 from dual;
  INSERT INTO event_facing_last ( last_facing ) select 3 from dual;
  INSERT INTO event_facing_last ( last_facing ) select 4 from dual;
  INSERT INTO event_facing_last ( last_facing ) select 5 from dual;
  INSERT INTO event_facing_last ( last_facing ) select 6 from dual;
  INSERT INTO event_facing_last ( last_facing ) select -1 from dual;
  INSERT INTO event_facing_last ( last_facing ) select -2 from dual;
  INSERT INTO event_facing_last ( last_facing ) select -3 from dual;

  SELECT MAX(event_choice_id) 
    INTO choiceID
    FROM event_choice_node;
  
  INSERT INTO event_choice ( event_choice_id, choice_facing, choice_target )
    SELECT choiceID, -1, 'Killed'
      FROM DUAL;

  INSERT INTO event_choice ( event_choice_id, choice_facing, choice_target )
    SELECT choiceID, -2, 'Starved'
      FROM DUAL;      

END INIT_TABLES;