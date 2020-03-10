-----------------------
--   REMOVE ORPHANS  --
-----------------------

-- delete from ARCH_CONTRACT_DATA_BACKUP if exists

DELETE FROM arch_contract_data_backup
WHERE KIND = 'PROCESS'
  AND tenantId = ?
  AND NOT EXISTS (
    SELECT ID FROM arch_process_instance b
    WHERE arch_contract_data.SCOPEID = b.ROOTPROCESSINSTANCEID
    AND b.tenantId = ?)
LIMIT ?;

DELETE FROM arch_contract_data_backup
WHERE KIND = 'TASK'
  AND tenantId = ?
  AND NOT EXISTS (
    SELECT ID FROM arch_flownode_instance b
    WHERE arch_contract_data_backup.SCOPEID = b.SOURCEOBJECTID
    AND b.tenantId = ?)
LIMIT ?;
