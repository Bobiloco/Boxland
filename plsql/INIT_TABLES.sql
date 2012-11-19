/**
	 * Boxland - Ant Colony Simulator 
	 * 
   * Init Tables - clears then populates the needed tables
	 * 
	 * Bernard McManus - 2012
	 * Source code under CC BY 3.0
	 */
CREATE OR REPLACE 
PROCEDURE INIT_TABLES AS 

 choiceID INTEGER;

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
  COMMIT;

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

  choiceID := event_choice_seq.currval;
  
  INSERT INTO event_choice ( event_choice_id, choice_facing, choice_target )
    SELECT choiceID, -1, 'Killed'
      FROM DUAL;

  INSERT INTO event_choice ( event_choice_id, choice_facing, choice_target )
    SELECT choiceID, -2, 'Starved'
      FROM DUAL;      

END INIT_TABLES;