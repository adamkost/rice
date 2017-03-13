--
-- Copyright 2005-2017 The Kuali Foundation
--
-- Licensed under the Educational Community License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.opensource.org/licenses/ecl2.php
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

DROP TABLE TRV_ACCT_TYPE
/
DROP TABLE TRV_ACCT
/
DROP TABLE TRV_ACCT_FO
/
DROP SEQUENCE TRV_FO_ID_S
/
DROP TABLE TRV_ACCT_EXT
/
CREATE TABLE TRV_ACCT_TYPE  ( 
	ACCT_TYPE     	VARCHAR2(3),
	ACCT_TYPE_NAME	VARCHAR2(40),
	OBJ_ID        	VARCHAR2(36) NOT NULL,
	VER_NBR       	NUMBER(8,0) DEFAULT 1 NOT NULL,
    PRIMARY KEY( ACCT_TYPE )
)
/
INSERT INTO TRV_ACCT_TYPE(ACCT_TYPE, ACCT_TYPE_NAME, OBJ_ID, VER_NBR)
  VALUES('CAT', 'Clearing', 'CAT', 1)
/
INSERT INTO TRV_ACCT_TYPE(ACCT_TYPE, ACCT_TYPE_NAME, OBJ_ID, VER_NBR)
  VALUES('EAT', 'Expense', 'EAT', 1)
/
INSERT INTO TRV_ACCT_TYPE(ACCT_TYPE, ACCT_TYPE_NAME, OBJ_ID, VER_NBR)
  VALUES('IAT', 'Income', 'IAT', 1)
/
CREATE TABLE TRV_ACCT  (
	ACCT_NUM      	VARCHAR2(10),
	OBJ_ID        	VARCHAR2(36) NOT NULL,
	VER_NBR       	NUMBER(8,0) DEFAULT 1 NOT NULL,
	ACCT_NAME     	VARCHAR2(40),
	ACCT_TYPE     	VARCHAR2(3),
	ACCT_FO_ID    	VARCHAR2(40),
	CREATE_DT     	DATE,
	SUBSIDIZED_PCT	NUMBER(5,2),
    PRIMARY KEY ( ACCT_NUM ),
    CONSTRAINT TRV_ACCT_TR1 FOREIGN KEY ( ACCT_TYPE ) REFERENCES TRV_ACCT_TYPE ( ACCT_TYPE )
)   
/
CREATE TABLE TRV_SUB_ACCT  ( 
	ACCT_NUM      	VARCHAR2(10),
	SUB_ACCT      	VARCHAR2(10),
	OBJ_ID        	VARCHAR2(36) NOT NULL,
	VER_NBR       	NUMBER(8,0) DEFAULT 1 NOT NULL,
	SUB_ACCT_NAME 	VARCHAR2(40),
    PRIMARY KEY ( ACCT_NUM, SUB_ACCT ),
    CONSTRAINT TRV_SUB_ACCT_TR1 FOREIGN KEY ( ACCT_NUM ) REFERENCES TRV_ACCT ( ACCT_NUM )
)
/

INSERT INTO TRV_ACCT(ACCT_NUM, ACCT_NAME, ACCT_TYPE, ACCT_FO_ID, OBJ_ID, CREATE_DT, SUBSIDIZED_PCT)
  VALUES('a1', 'Travel Account 1', 'IAT', 'fred', 'a1', NULL, NULL)
/
INSERT INTO TRV_ACCT(ACCT_NUM, ACCT_NAME, ACCT_TYPE, ACCT_FO_ID, OBJ_ID, CREATE_DT, SUBSIDIZED_PCT)
  VALUES('a14', 'Travel Account 14', 'CAT', 'fran', 'a14', NULL, NULL)
/
INSERT INTO TRV_ACCT(ACCT_NUM, ACCT_NAME, ACCT_TYPE, ACCT_FO_ID, OBJ_ID, CREATE_DT, SUBSIDIZED_PCT)
  VALUES('a2', 'Travel Account 2', 'EAT', 'fran', 'a2', NULL, NULL)
/
INSERT INTO TRV_ACCT(ACCT_NUM, ACCT_NAME, ACCT_TYPE, ACCT_FO_ID, OBJ_ID, CREATE_DT, SUBSIDIZED_PCT)
  VALUES('a3', 'Travel Account 3', 'IAT', 'frank', 'a3', NULL, 20.0)
/
INSERT INTO TRV_ACCT(ACCT_NUM, ACCT_NAME, ACCT_TYPE, ACCT_FO_ID, OBJ_ID, CREATE_DT, SUBSIDIZED_PCT)
  VALUES('a6', 'Travel Account 6', 'CAT', 'fran', 'a6', NULL, NULL)
/
INSERT INTO TRV_ACCT(ACCT_NUM, ACCT_NAME, ACCT_TYPE, ACCT_FO_ID, OBJ_ID, CREATE_DT, SUBSIDIZED_PCT)
  VALUES('a8', 'Travel Account 8', 'EAT', 'fran', 'a8', NULL, NULL)
/
INSERT INTO TRV_ACCT(ACCT_NUM, ACCT_NAME, ACCT_TYPE, ACCT_FO_ID, OBJ_ID, CREATE_DT, SUBSIDIZED_PCT)
  VALUES('a9', 'Travel Account 9', 'CAT', 'fran', 'a9', NULL, NULL)
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a14', 'a', 'a', 'Sub Account A')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a14', 'b', 'b', 'Sub Account B')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a14', 'c', 'c', 'Sub Account C')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a14', 'd', 'd', 'Sub Account D')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a14', 'e', 'e', 'Sub Account E')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a14', 'f', 'f', 'Sub Account F')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a14', 'g', 'g', 'Sub Account G')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a14', 'h', 'h', 'Sub Account H')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a14', 'i', 'i', 'Sub Account Eye')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a14', 'j', 'j', 'Sub Account J')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a14', 'k', 'k', 'Sub Account K')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a14', 'l', 'l', 'Sub Account L')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a14', 'm', 'm', 'Sub Account M')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a14', 'n', 'n', 'Sub Account N')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a14', 'sub123', 'sub123', 'Sub Account 123')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a2', 'sub1', 'a2-sub1', 'Sub Account 1')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a2', 'sub2', 'a2-sub2', 'Sub Account 2')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a3', 'sub1', 'a3-sub1', 'Sub Account 1')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a3', 'sub2', 'a3-sub2', 'Sub Account 2')
/
INSERT INTO TRV_SUB_ACCT(ACCT_NUM, SUB_ACCT, OBJ_ID, SUB_ACCT_NAME)
  VALUES('a3', 'sub3', 'a3-sub3', 'Sub Account 3')
/
