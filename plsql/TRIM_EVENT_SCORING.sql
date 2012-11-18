/**
	 * Boxland - Ant Colony Simulator 
	 * 
   * Trim_Event_Scoring.sql - Chops out some old memories
	 * 
	 * Bernard McManus - 2012
	 * Source code under CC BY 3.0
	 */
create or replace 
PROCEDURE TRIM_EVENT_SCORING AS 

maxRow INT;

BEGIN
  
  SELECT MAX(EVENT_HIST_ID) INTO maxRow FROM EVENT_HIST;

  -- They only remember the last 10k actions made, which should be less actual bad choices? maybe?
  DELETE FROM EVENT_SCORING WHERE EVENT_HIST_ID < ( maxRow - 6000 );
  DELETE FROM EVENT_HIST WHERE EVENT_HIST_ID < ( maxRow - 6000 );

END TRIM_EVENT_SCORING;