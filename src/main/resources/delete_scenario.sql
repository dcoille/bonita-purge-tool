-- First delete matching cases (outside this script)

-- Then:
-----------------------
--   REMOVE ORPHANS  --
-----------------------

-- ARCH_CONTRACT_DATA
-- Depends on PROCESS INSTANCE (ROOTPROCESSINSTANCEID)
DELETE FROM arch_contract_data a
WHERE a.KIND = 'PROCESS'
AND a.tenantId = ?
AND NOT EXISTS (
    SELECT ID FROM arch_process_instance b
    WHERE a.SCOPEID = b.ROOTPROCESSINSTANCEID
    AND b.tenantId = ?);

-- ARCH_DATA_INSTANCE
-- Depends on PROCESS INSTANCE (ROOTPROCESSINSTANCEID)
DELETE FROM arch_data_instance a WHERE
a.CONTAINERTYPE = 'PROCESS_INSTANCE'
AND a.tenantId = ?
AND NOT EXISTS (
    SELECT id FROM arch_process_instance b
    WHERE a.CONTAINERID = b.ROOTPROCESSINSTANCEID
    AND b.tenantId = ? ) ;

-- ARCH_DOCUMENT_MAPPING
-- Depends on PROCESS INSTANCE (PROCESSINSTANCEID)
DELETE FROM arch_document_mapping a WHERE a.tenantId = ?
AND NOT EXISTS (
    SELECT ID FROM arch_process_instance b
    WHERE a.PROCESSINSTANCEID = b.SOURCEOBJECTID
    AND b.tenantId = ?  );

-- ARCH_FLOWNODE_INSTANCE
-- Depends on PROCESS INSTANCE (ROOTPROCESSINSTANCEID)
DELETE FROM arch_flownode_instance a WHERE a.tenantId = ?
AND NOT EXISTS (
    SELECT id FROM arch_process_instance b
    WHERE a.ROOTCONTAINERID = b.ROOTPROCESSINSTANCEID
    AND b.tenantId = ? );

-- ARCH_PROCESS_COMMENT
-- Depends on PROCESS INSTANCE (PROCESSINSTANCEID)
DELETE FROM arch_process_comment a WHERE  a.tenantId = ?
AND NOT EXISTS (
    SELECT ID FROM arch_process_instance b
    WHERE a.PROCESSINSTANCEID = b.SOURCEOBJECTID
    AND b.tenantId = ? );

-- ARCH_REF_BIZ_DATA_INST
-- Depends on PROCESS INSTANCE (PROCESSINSTANCEID)
DELETE FROM arch_ref_biz_data_inst a WHERE a.tenantId = ?
AND NOT EXISTS (
    SELECT ID FROM arch_process_instance b
    WHERE a.ORIG_PROC_INST_ID = b.SOURCEOBJECTID
    AND b.tenantId = ?  );

-- ARCH_CONNECTOR_INSTANCE
-- Depends on PROCESS INSTANCE (ROOTPROCESSINSTANCEID)
DELETE FROM arch_connector_instance a WHERE a.CONTAINERTYPE = 'process'
AND a.tenantId = ?
AND NOT EXISTS (
    SELECT ID FROM arch_process_instance b
    WHERE a.CONTAINERID = b.SOURCEOBJECTID
    AND b.tenantId = ?);

-- ARCH_CONTRACT_DATA (2)
-- Depends on FLOWNODE INSTANCE
DELETE FROM arch_contract_data a
WHERE a.KIND = 'TASK'
  AND a.tenantId = ?
  AND NOT EXISTS (
    SELECT ID FROM arch_flownode_instance b
    WHERE a.SCOPEID = b.SOURCEOBJECTID
    AND b.tenantId = ? );

-- ARCH_DATA_INSTANCE (2)
-- Depends on FLOWNODE INSTANCE
DELETE FROM arch_data_instance a WHERE a.CONTAINERTYPE = 'ACTIVITY_INSTANCE'
AND a.tenantId = ?
AND NOT EXISTS (
    SELECT id FROM arch_flownode_instance b
    WHERE a.CONTAINERID = b.SOURCEOBJECTID
    AND b.tenantId = ? );

-- ARCH_CONNECTOR_INSTANCE (2)
-- Depends on FLOWNODE INSTANCE
DELETE FROM arch_connector_instance a WHERE a.CONTAINERTYPE = 'flowNode'
AND  a.tenantId = ?
AND NOT EXISTS (
    SELECT ID FROM arch_flownode_instance b
    WHERE a.CONTAINERID = b.SOURCEOBJECTID
    AND b.tenantId = ? );

-- ARCH_MULTI_BIZ_DATA
-- Depends on ARCH_REF_BIZ_DATA_INST
-- !!!! NO NEED FOR SPECIFIC DELETION STATEMENT, AS DELETION IS HANDLED
-- BY ON_DELETE_CASCADE INSTRUCTION !!!!