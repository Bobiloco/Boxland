/* Install the database tables */

DROP INDEX EVENT_HIST_ECN_IDX;
DROP INDEX EVENT_CHOICE_HIST_IDX;
DROP INDEX EVENT_CHOICE_NODE_IDX;
DROP INDEX OBJ_IDX;
DROP INDEX EVENT_DECISION_IDX;

DROP TRIGGER EVENT_HIST_TIBR;
DROP TRIGGER OBJ_TIBR;
DROP TRIGGER EVENT_DECISION_TIBR;
DROP TRIGGER EVENT_CHOICE_NODE_TIBR;
DROP TRIGGER EVENT_LOC_TIBR;

DROP SEQUENCE EVENT_HIST_SEQ;
DROP SEQUENCE OBJ_SEQ;
DROP SEQUENCE EVENT_DECISION_SEQ;
DROP SEQUENCE EVENT_CHOICE_SEQ;
DROP SEQUENCE EVENT_LOC_SEQ;

DROP TABLE EVENT_SCORING;
DROP TABLE EVENT_HIST;
DROP TABLE EVENT_DECISION;
DROP TABLE EVENT_LOC;
DROP TABLE EVENT_CHOICE;
DROP TABLE EVENT_CHOICE_NODE;
DROP TABLE OBJ;

CREATE TABLE OBJ
( OBJ_ID   NUMBER NOT NULL,
  OBJ_CD   NUMBER NOT NULL,
  OBJ_TEAM VARCHAR2(10)
, CONSTRAINT OBJ_PK PRIMARY KEY ( OBJ_ID ) ENABLE );
/

CREATE TABLE EVENT_CHOICE_NODE
( EVENT_CHOICE_ID NUMBER NOT NULL
, CT0             VARCHAR2(10)
, CT1             VARCHAR2(10)
, CT2             VARCHAR2(10)
, CT3             VARCHAR2(10)
, CT4             VARCHAR2(10)
, CT5             VARCHAR2(10)
, CT6             VARCHAR2(10)
, CONSTRAINT EVENT_CHOICE_NODE_PK PRIMARY KEY ( EVENT_CHOICE_ID ) ENABLE );
/

CREATE TABLE EVENT_CHOICE
( EVENT_CHOICE_ID NUMBER NOT NULL
, CHOICE_FACING   NUMBER NOT NULL
, CHOICE_TARGET   VARCHAR2(10)
, CONSTRAINT EVENT_CHOICE_PK PRIMARY KEY ( EVENT_CHOICE_ID, CHOICE_FACING ) ENABLE
, CONSTRAINT EVENT_CHOICE_ECN_FK FOREIGN KEY ( EVENT_CHOICE_ID ) 
    REFERENCES EVENT_CHOICE_NODE ( EVENT_CHOICE_ID ) ENABLE );
/

CREATE TABLE EVENT_LOC
( EVENT_LOC_ID NUMBER NOT NULL
, LOC_X        NUMBER NOT NULL
, LOC_Y        NUMBER NOT NULL
, LOC_Z        NUMBER NOT NULL
, CONSTRAINT EVENT_LOC_PK PRIMARY KEY ( EVENT_LOC_ID ) ENABLE );
/

CREATE TABLE EVENT_DECISION
( EVENT_DECISION_ID NUMBER NOT NULL
, OBJ_ID            NUMBER NOT NULL
, EVENT_CHOICE_ID   NUMBER NOT NULL
, EVENT_LOC_ID      NUMBER NOT NULL
, CONSTRAINT EVENT_DECISION_PK PRIMARY KEY ( EVENT_DECISION_ID ) ENABLE
, CONSTRAINT EVENT_DECISION_ECN_FK FOREIGN KEY ( EVENT_CHOICE_ID )
    REFERENCES EVENT_CHOICE_NODE ( EVENT_CHOICE_ID ) ENABLE
, CONSTRAINT EVENT_CHOICE_LOC_FK FOREIGN KEY ( EVENT_LOC_ID ) 
    REFERENCES EVENT_LOC ( EVENT_LOC_ID ) ENABLE );
/

CREATE TABLE EVENT_HIST
( EVENT_HIST_ID     NUMBER NOT NULL 
, EVENT_DECISION_ID NUMBER NOT NULL 
, EVENT_TYPE        VARCHAR2(10) NOT NULL
, CHOICE_FACING     NUMBER NOT NULL
, CONSTRAINT EVENT_HIST_PK PRIMARY KEY ( EVENT_HIST_ID ) ENABLE
, CONSTRAINT EVENT_HIST_DEC_FK FOREIGN KEY ( EVENT_DECISION_ID )
    REFERENCES EVENT_DECISION ( EVENT_DECISION_ID ) ENABLE );
/

CREATE TABLE EVENT_SCORING
( EVENT_HIST_ID NUMBER NOT NULL 
, CONSTRAINT EVENT_SCORING_FK FOREIGN KEY ( EVENT_HIST_ID ) 
    REFERENCES EVENT_HIST ( EVENT_HIST_ID ) ENABLE );
/

CREATE SEQUENCE EVENT_HIST_SEQ INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE OBJ_SEQ INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE EVENT_DECISION_SEQ INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE EVENT_CHOICE_SEQ INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE EVENT_LOC_SEQ INCREMENT BY 1 START WITH 1;
/

CREATE OR REPLACE TRIGGER EVENT_HIST_TIBR 
BEFORE INSERT ON EVENT_HIST 
FOR EACH ROW 
BEGIN
  select EVENT_HIST_SEQ.nextval into :new.EVENT_HIST_ID from dual;
END;
/

CREATE OR REPLACE TRIGGER OBJ_TIBR 
BEFORE INSERT ON Obj 
FOR EACH ROW 
BEGIN
  select OBJ_SEQ.nextval into :new.OBJ_ID from dual;
end;
/

CREATE OR REPLACE TRIGGER EVENT_DECISION_TIBR 
BEFORE INSERT ON EVENT_DECISION 
FOR EACH ROW
BEGIN
  select EVENT_DECISION_SEQ.nextval into :new.EVENT_DECISION_ID from dual;
END;
/

CREATE OR REPLACE TRIGGER EVENT_CHOICE_NODE_TIBR 
BEFORE INSERT ON EVENT_CHOICE_NODE 
FOR EACH ROW 
BEGIN
  select EVENT_CHOICE_SEQ.nextval into :new.EVENT_CHOICE_ID from dual;
END;
/

CREATE OR REPLACE TRIGGER EVENT_LOC_TIBR 
BEFORE INSERT ON EVENT_LOC
FOR EACH ROW 
BEGIN
  select EVENT_LOC_SEQ.nextval into :new.EVENT_LOC_ID from dual;
END;
/

CREATE INDEX EVENT_HIST_ECN_IDX ON EVENT_HIST ( EVENT_HIST_ID, EVENT_TYPE, CHOICE_FACING );
CREATE INDEX EVENT_CHOICE_HIST_IDX ON EVENT_CHOICE ( EVENT_CHOICE_ID, CHOICE_FACING, CHOICE_TARGET );
CREATE INDEX EVENT_CHOICE_NODE_IDX ON EVENT_CHOICE_NODE ( EVENT_CHOICE_ID, CT0, CT1, CT2, CT3, CT4, CT5, CT6 );
CREATE INDEX OBJ_IDX ON OBJ ( OBJ_ID, OBJ_TEAM );
CREATE INDEX EVENT_DECISION_IDX ON EVENT_DECISION ( EVENT_DECISION_ID, OBJ_ID, EVENT_CHOICE_ID );
CREATE INDEX EVENT_LOC_IDX ON EVENT_LOC ( EVENT_LOC_ID, LOC_X, LOC_Y, LOC_Z );
/

/*
BEGIN
DBMS_STATS.GATHER_SCHEMA_STATS('Terre');
END;
/
comment on table EVENT_HIST is '';
comment on table EVENT_CHOICE is '';
comment on table EVENT_SCORING is '';
comment on table EVENT_CHOICE_NODE is '';
comment on table OBJ is '';
*/