-----------------------
--   REMOVE ORPHANS  --
-----------------------

-- delete from ARCH_CONTRACT_DATA_BACKUP if exists

DELETE FROM ARCH_CONTRACT_DATA_BACKUP a
WHERE a.tenantId = ?
AND NOT EXISTS (
    SELECT ID FROM ARCH_PROCESS_INSTANCE b
    WHERE a.SCOPEID = b.ROOTPROCESSINSTANCEID
    AND b.tenantId = ?);
