-- First retrieve cases to be deleted.

-- Root cases archives IN a given date
-- SELECT ROOTPROCESSINSTANCEID AS archCase FROM ARCH_PROCESS_INSTANCE
-- WHERE ROOTPROCESSINSTANCEID = SOURCEOBJECTID AND STATEID = 6 AND ENDDATE > 1;

--DELETE (FOR EACH) archCase

DELETE FROM ARCH_PROCESS_INSTANCE A
WHERE A.ROOTPROCESSINSTANCEID = (
    SELECT B.ROOTPROCESSINSTANCEID
    FROM ARCH_PROCESS_INSTANCE B
    WHERE B.PROCESSDEFINITIONID = ?
    AND B.ENDDATE > ?
);



-----------------------
--   REMOVE ORPHANS  --
-----------------------

-- ARCH_CONTRACT_DATA
-- Depends on PROCESS INSTANCE (ROOTPROCESSINSTANCEID)
-- Depends on FLOWNODE INSTANCE (Handle later)

SELECT a.*
FROM ARCH_CONTRACT_DATA a
LEFT JOIN ARCH_PROCESS_INSTANCE b
ON a.SCOPEID = b.ROOTPROCESSINSTANCEID
WHERE a.KIND = 'PROCESS'
AND b.ROOTPROCESSINSTANCEID IS NULL;


-- ARCH_DATA_INSTANCE
-- Depends on PROCESS INSTANCE (ROOTPROCESSINSTANCEID)
-- Depends on FLOWNODE INSTANCE (Handle later)

SELECT a.*
FROM ARCH_DATA_INSTANCE a
LEFT JOIN ARCH_PROCESS_INSTANCE b
ON a.CONTAINERID = b.ROOTPROCESSINSTANCEID
WHERE a.CONTAINERTYPE = 'PROCESS_INSTANCE'
AND b.ROOTPROCESSINSTANCEID IS NULL;


-- ARCH_DOCUMENT_MAPPING
-- Depends on PROCESS INSTANCE (PROCESSINSTANCEID)

SELECT a.*
FROM ARCH_DOCUMENT_MAPPING a
LEFT JOIN ARCH_PROCESS_INSTANCE b
ON a.PROCESSINSTANCEID = b.SOURCEOBJECTID
WHERE b.SOURCEOBJECTID IS NULL;


-- ARCH_FLOWNODE_INSTANCE
-- Depends on PROCESS INSTANCE (ROOTPROCESSINSTANCEID)

SELECT a.*
FROM ARCH_FLOWNODE_INSTANCE a
LEFT JOIN ARCH_PROCESS_INSTANCE b
ON a.ROOTCONTAINERID = b.ROOTPROCESSINSTANCEID
WHERE b.ROOTPROCESSINSTANCEID IS NULL;


-- ARCH_PROCESS_COMMENT
-- Depends on PROCESS INSTANCE (PROCESSINSTANCEID)

SELECT a.*
FROM ARCH_PROCESS_COMMENT a
LEFT JOIN ARCH_PROCESS_INSTANCE b
ON a.PROCESSINSTANCEID = b.SOURCEOBJECTID
WHERE b.SOURCEOBJECTID IS NULL;


-- ARCH_REF_BIZ_DATA_INST
-- Depends on PROCESS INSTANCE (PROCESSINSTANCEID)
-- TO VALIDATE: ORIG_FN_INST_ID

SELECT a.*
FROM ARCH_REF_BIZ_DATA_INST a
LEFT JOIN ARCH_PROCESS_INSTANCE b
ON a.ORIG_PROC_INST_ID = b.SOURCEOBJECTID
WHERE b.SOURCEOBJECTID IS NULL;


-- ARCH_CONNECTOR_INSTANCE
-- Depends on PROCESS INSTANCE (ROOTPROCESSINSTANCEID)
-- Depends on FLOWNODE INSTANCE (Handle later)

SELECT a.*
FROM ARCH_CONNECTOR_INSTANCE a
LEFT JOIN ARCH_PROCESS_INSTANCE  b
ON a.CONTAINERID = b.ROOTPROCESSINSTANCEID
WHERE a.CONTAINERTYPE = 'process'
AND b.ROOTPROCESSINSTANCEID IS NULL;



-- ARCH_CONTRACT_DATA (2)
-- Depends on PROCESS INSTANCE (Handled before)
-- Depends on FLOWNODE INSTANCE (Handle later)

SELECT a.*
FROM ARCH_CONTRACT_DATA a
LEFT JOIN ARCH_FLOWNODE_INSTANCE b
ON a.SCOPEID = b.SOURCEOBJECTID
WHERE a.KIND = 'TASK'
AND b.SOURCEOBJECTID  IS NULL;


-- ARCH_DATA_INSTANCE (2)
-- Depends on PROCESS INSTANCE (Handled before)
-- Depends on FLOWNODE INSTANCE (Handle later)

SELECT a.*
FROM ARCH_DATA_INSTANCE a
LEFT JOIN ARCH_FLOWNODE_INSTANCE b
ON a.CONTAINERID = b.SOURCEOBJECTID
WHERE a.CONTAINERTYPE = 'ACTIVITY_INSTANCE'
AND b.SOURCEOBJECTID  IS NULL;



-- ARCH_CONNECTOR_INSTANCE (2)
-- Depends on PROCESS INSTANCE (Handled before)
-- Depends on FLOWNODE INSTANCE (Handle later)

SELECT a.*
FROM ARCH_CONNECTOR_INSTANCE a
LEFT JOIN ARCH_FLOWNODE_INSTANCE b
ON a.CONTAINERID = b.SOURCEOBJECTID
WHERE a.CONTAINERTYPE = 'flowNode'
AND b.SOURCEOBJECTID IS NULL;


-- ARCH_MULTI_BIZ_DATA
-- Depends on ARCH_REF_BIZ_DATA_INST (Handled before)

SELECT a.*
FROM ARCH_MULTI_BIZ_DATA a
LEFT JOIN ARCH_REF_BIZ_DATA_INST b
ON a.ID = b.ID
WHERE b.ID IS NULL;


--UTILS
-- UPDATE ARCH_PROCESS_INSTANCE SET ROOTPROCESSINSTANCEID =55 WHERE ROOTPROCESSINSTANCEID = 1001
-- UPDATE ARCH_PROCESS_INSTANCE SET SOURCEOBJECTID = SOURCEOBJECTID*10 WHERE ROOTPROCESSINSTANCEID = 55
-- UPDATE ARCH_FLOWNODE_INSTANCE SET SOURCEOBJECTID = SOURCEOBJECTID*10