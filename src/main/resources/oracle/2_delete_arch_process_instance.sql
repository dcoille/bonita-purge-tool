DELETE FROM ARCH_PROCESS_INSTANCE A WHERE exists (
SELECT rootprocessinstanceid
FROM ARCH_PROCESS_INSTANCE B
WHERE B.ROOTPROCESSINSTANCEID = B.SOURCEOBJECTID
AND A.ROOTPROCESSINSTANCEID = B.ROOTPROCESSINSTANCEID
AND PROCESSDEFINITIONID = ?
and (STATEID = 6 OR STATEID = 3 OR STATEID = 4)
AND ENDDATE <= ?) AND tenantId = ?;