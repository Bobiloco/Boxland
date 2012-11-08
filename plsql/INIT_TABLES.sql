create or replace 
PROCEDURE INIT_TABLES AS 

 choiceID INT;

BEGIN
  DELETE FROM EVENT_SCORING;
  DELETE FROM EVENT_HIST;
	DELETE FROM EVENT_DECISION;
	DELETE FROM EVENT_CHOICE;
  DELETE FROM EVENT_CHOICE_NODE;
	DELETE FROM OBJ;
  
  -- Create the 'Killed' dummy choice row
  INSERT INTO event_choice_node ( event_choice_id, ct0, ct1, ct2, ct3, ct4, ct5, ct6 )
    SELECT 0, ' ', ' ', ' ', ' ', ' ', ' ', ' ' FROM DUAL;
    
  SELECT MAX(event_choice_id) 
    INTO choiceID
    FROM event_choice_node;
  
  INSERT INTO event_choice ( event_choice_id, choice_facing, choice_target, choice_orig_x, choice_orig_y, choice_orig_z, choice_dir_x, choice_dir_y, choice_dir_z )
    SELECT choiceID, 0, 'Killed', null, null, null, null, null, null
      FROM DUAL;

END INIT_TABLES;