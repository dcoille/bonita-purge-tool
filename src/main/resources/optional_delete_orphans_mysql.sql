-----------------------
--   REMOVE ORPHANS  --
-----------------------

-- delete from ARCH_CONTRACT_DATA_BACKUP if exists

DELETE a FROM arch_contract_data_backup a
WHERE a.tenantId = ?
AND NOT EXISTS (
    SELECT ID FROM arch_process_instance b
    WHERE a.SCOPEID = b.ROOTPROCESSINSTANCEID
AND b.tenantId = ?);
