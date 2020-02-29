-- First delete matching cases (outside this script)

-- Then:
-----------------------
--   REMOVE ORPHANS  --
-----------------------

-- ARCH_CONTRACT_DATA
-- Depends on PROCESS INSTANCE (ROOTPROCESSINSTANCEID)
DELETE a FROM arch_contract_data as a
WHERE a.kind = 'PROCESS'
AND a.tenantId = ?
AND NOT EXISTS (
    SELECT ID FROM arch_process_instance b
    WHERE a.scopeid = b.rootprocessinstanceid
    AND b.tenantId = ?);

-- ARCH_DATA_INSTANCE
-- Depends on PROCESS INSTANCE (ROOTPROCESSINSTANCEID)
DELETE a  FROM arch_data_instance as a WHERE
a.containertype = 'PROCESS_INSTANCE'
AND a.tenantId = ?
AND NOT EXISTS (
    SELECT id FROM arch_process_instance b
    WHERE a.containerid = b.rootprocessinstanceid
    AND b.tenantId = ? ) ;

-- ARCH_DOCUMENT_MAPPING
-- Depends on PROCESS INSTANCE (PROCESSINSTANCEID)
DELETE a FROM arch_document_mapping as a WHERE a.tenantId = ?
AND NOT EXISTS (
    SELECT ID FROM arch_process_instance b
    WHERE a.processinstanceid = b.sourceobjectid
    AND b.tenantId = ?  );

-- ARCH_FLOWNODE_INSTANCE
-- Depends on PROCESS INSTANCE (ROOTPROCESSINSTANCEID)
DELETE a FROM arch_flownode_instance as a WHERE a.tenantId = ?
AND NOT EXISTS (
    SELECT id FROM arch_process_instance b
    WHERE a.rootcontainerid = b.rootprocessinstanceid
    AND b.tenantId = ? );

-- ARCH_PROCESS_COMMENT
-- Depends on PROCESS INSTANCE (PROCESSINSTANCEID)
DELETE a  FROM arch_process_comment as a WHERE  a.tenantId = ?
AND NOT EXISTS (
    SELECT ID FROM arch_process_instance b
    WHERE a.processinstanceid = b.sourceobjectid
    AND b.tenantId = ? );

-- ARCH_REF_BIZ_DATA_INST
-- Depends on PROCESS INSTANCE (PROCESSINSTANCEID)
DELETE a FROM arch_ref_biz_data_inst as a WHERE a.tenantId = ?
AND NOT EXISTS (
    SELECT id FROM arch_process_instance b
    WHERE a.orig_proc_inst_id = b.sourceobjectid
    AND b.tenantId = ?  );

-- ARCH_CONNECTOR_INSTANCE
-- Depends on PROCESS INSTANCE (ROOTPROCESSINSTANCEID)
DELETE a FROM arch_connector_instance as a WHERE a.containertype = 'process'
AND a.tenantId = ?
AND NOT EXISTS (
    SELECT id FROM arch_process_instance b
    WHERE a.containerid = b.sourceobjectid
    AND b.tenantId = ?);

-- ARCH_CONTRACT_DATA (2)
-- Depends on FLOWNODE INSTANCE
DELETE a FROM arch_contract_data as a
WHERE a.KIND = 'TASK'
  AND a.tenantId = ?
  AND NOT EXISTS (
    SELECT id FROM arch_flownode_instance b
    WHERE a.scopeid = b.sourceobjectid
    AND b.tenantId = ? );

-- ARCH_DATA_INSTANCE (2)
-- Depends on FLOWNODE INSTANCE
DELETE a FROM arch_data_instance as a WHERE a.containertype = 'ACTIVITY_INSTANCE'
AND a.tenantId = ?
AND NOT EXISTS (
    SELECT id FROM arch_flownode_instance b
    WHERE a.containerid = b.sourceobjectid
    AND b.tenantId = ? );

-- ARCH_CONNECTOR_INSTANCE (2)
-- Depends on FLOWNODE INSTANCE
DELETE a FROM arch_connector_instance a WHERE a.containertype = 'flowNode'
AND  a.tenantId = ?
AND NOT EXISTS (
    SELECT ID FROM arch_flownode_instance b
    where a.containerid = b.sourceobjectid
    AND b.tenantId = ? );

-- ARCH_MULTI_BIZ_DATA
-- Depends on ARCH_REF_BIZ_DATA_INST
-- !!!! NO NEED FOR SPECIFIC DELETION STATEMENT, AS DELETION IS HANDLED
-- BY ON_DELETE_CASCADE INSTRUCTION !!!!