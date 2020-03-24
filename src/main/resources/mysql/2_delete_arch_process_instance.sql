DELETE A FROM arch_process_instance A
INNER JOIN arch_process_instance B ON A.rootprocessinstanceid = B.rootprocessinstanceid
WHERE B.rootprocessinstanceid = B.sourceobjectid
AND B.processdefinitionid = ?
AND (B.stateid = 6 OR B.stateid = 3 OR B.stateid = 4)
AND B.enddate <= ? AND B.tenantId = ?;
