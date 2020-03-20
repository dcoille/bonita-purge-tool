-- First delete matching cases (outside this script)

-- Then:
-----------------------
--   REMOVE ORPHANS  --
-----------------------

-- CREATE INDEX idx_rootprocid_archprocinst_tmp ON arch_process_instance(rootprocessinstanceid);

-- ARCH_CONTRACT_DATA
-- Depends on PROCESS INSTANCE (ROOTPROCESSINSTANCEID)
DELETE FROM arch_contract_data
WHERE kind = 'PROCESS'
AND tenantId = ?
AND NOT EXISTS (
    SELECT ID FROM arch_process_instance b
    WHERE arch_contract_data.scopeid = b.rootprocessinstanceid
    AND b.tenantId = ?)
LIMIT ?;

-- ARCH_DATA_INSTANCE
-- Depends on PROCESS INSTANCE (ROOTPROCESSINSTANCEID)
DELETE FROM arch_data_instance
WHERE arch_data_instance.containertype = 'PROCESS_INSTANCE'
AND arch_data_instance.tenantId = ?
AND NOT EXISTS (
    SELECT id FROM arch_process_instance b
    WHERE arch_data_instance.containerid = b.rootprocessinstanceid
    AND b.tenantId = ?)
LIMIT ?;

-- ARCH_DOCUMENT_MAPPING
-- Depends on PROCESS INSTANCE (PROCESSINSTANCEID)
DELETE FROM arch_document_mapping
WHERE tenantId = ?
AND NOT EXISTS (
    SELECT ID FROM arch_process_instance b
    WHERE arch_document_mapping.processinstanceid = b.sourceobjectid
    AND b.tenantId = ?)
LIMIT ?;

-- ARCH_FLOWNODE_INSTANCE
-- Depends on PROCESS INSTANCE (ROOTPROCESSINSTANCEID)
DELETE FROM arch_flownode_instance
WHERE tenantId = ?
AND NOT EXISTS (
    SELECT id FROM arch_process_instance b
    WHERE arch_flownode_instance.rootcontainerid = b.rootprocessinstanceid
    AND b.tenantId = ?)
LIMIT ?;

-- ARCH_PROCESS_COMMENT
-- Depends on PROCESS INSTANCE (PROCESSINSTANCEID)
DELETE FROM arch_process_comment
WHERE tenantId = ?
AND NOT EXISTS (
    SELECT ID FROM arch_process_instance b
    WHERE arch_process_comment.processinstanceid = b.sourceobjectid
    AND b.tenantId = ?)
LIMIT ?;

-- ARCH_REF_BIZ_DATA_INST
-- Depends on PROCESS INSTANCE (PROCESSINSTANCEID)
DELETE FROM arch_ref_biz_data_inst
WHERE tenantId = ?
AND NOT EXISTS (
    SELECT id FROM arch_process_instance b
    WHERE arch_ref_biz_data_inst.orig_proc_inst_id = b.sourceobjectid
    AND b.tenantId = ?)
LIMIT ?;

-- ARCH_CONNECTOR_INSTANCE
-- Depends on PROCESS INSTANCE (ROOTPROCESSINSTANCEID)
DELETE FROM arch_connector_instance
WHERE containertype = 'process'
AND tenantId = ?
AND NOT EXISTS (
    SELECT id FROM arch_process_instance b
    WHERE arch_connector_instance.containerid = b.sourceobjectid
    AND b.tenantId = ?)
LIMIT ?;

-- ARCH_CONTRACT_DATA (2)
-- Depends on FLOWNODE INSTANCE
DELETE FROM arch_contract_data
WHERE KIND = 'TASK'
  AND tenantId = ?
  AND NOT EXISTS (
    SELECT id FROM arch_flownode_instance b
    WHERE arch_contract_data.scopeid = b.sourceobjectid
    AND b.tenantId = ?)
LIMIT ?;

-- ARCH_DATA_INSTANCE (2)
-- Depends on FLOWNODE INSTANCE
DELETE FROM arch_data_instance
WHERE containertype = 'ACTIVITY_INSTANCE'
AND tenantId = ?
AND NOT EXISTS (
    SELECT id FROM arch_flownode_instance b
    WHERE arch_data_instance.containerid = b.sourceobjectid
    AND b.tenantId = ?)
LIMIT ?;

-- ARCH_CONNECTOR_INSTANCE (2)
-- Depends on FLOWNODE INSTANCE
DELETE FROM arch_connector_instance
WHERE containertype = 'flowNode'
AND tenantId = ?
AND NOT EXISTS (
    SELECT ID FROM arch_flownode_instance b
    where arch_connector_instance.containerid = b.sourceobjectid
    AND b.tenantId = ?)
LIMIT ?;

-- DROP INDEX idx_rootprocid_archprocinst_tmp ON arch_process_instance;

-- ARCH_MULTI_BIZ_DATA
-- Depends on ARCH_REF_BIZ_DATA_INST
-- !!!! NO NEED FOR SPECIFIC DELETION STATEMENT, AS DELETION IS HANDLED
-- BY ON_DELETE_CASCADE INSTRUCTION !!!!