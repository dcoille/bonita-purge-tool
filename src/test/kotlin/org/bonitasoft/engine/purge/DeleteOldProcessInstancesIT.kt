package org.bonitasoft.engine.purge

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.BeforeTest

/**
 * @author Emmanuel Duchastenier
 */
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SpringBootTest
class DeleteOldProcessInstancesIT {

    @Autowired
    lateinit var deleteOldProcessInstances: DeleteOldProcessInstances

    @Value("\${spring.datasource.driver-class-name:org.postgresql.Driver}")
    lateinit var driverClassName: String

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private var archiveRepository = ArchiveRepository()

    private val logger = LoggerFactory.getLogger(DeleteOldProcessInstancesIT::class.java)

    @BeforeTest
    fun before() {
        transaction {
            logger.info("Drop test tables")
            SchemaUtils.drop()
        }
        jdbcTemplate.dataSource?.connection.use { c ->
            // to make sure the connection is released afterwards
            logger.debug("jdbcTemplate connection url: ${c?.metaData?.url}")
        }
        when (val dbVendor = getDbVendor(driverClassName)) {
            "sqlserver" -> createTablesFromScript("/sqlserver.sql", "GO")
            else ->
                createTablesFromScript("/$dbVendor.sql")
        }
        transaction {
            archiveRepository.insert_data_before_purge()
        }
    }

    private fun createTablesFromScript(fileName: String, delimiter: String = ";") {
        logger.info("Creating tables using script $fileName")
        val content = DeleteOldProcessInstancesIT::class.java.getResource(fileName).readText()
        content.split(delimiter).filter { !it.isBlank() }.forEach {
            jdbcTemplate.execute(it.trim())
        }
    }

    @Test
    fun `should delete records of a process before a timestamp and leave records of other processes and records after the timestamp`() {

        // given:
        val rowsNotToDelete = """
select count(id) FROM arch_process_instance A WHERE not exists (
	SELECT rootprocessinstanceid
	FROM arch_process_instance B
	WHERE B.ROOTPROCESSINSTANCEID = B.SOURCEOBJECTID
    AND PROCESSDEFINITIONID = ?
	AND A.ROOTPROCESSINSTANCEID = B.ROOTPROCESSINSTANCEID
	and (STATEID = 6 OR STATEID = 3 OR STATEID = 4) AND ENDDATE <= ?)"""

        val rowsToDelete = """
select count(id) FROM arch_process_instance A WHERE exists (
	SELECT rootprocessinstanceid
	FROM arch_process_instance B
	WHERE B.ROOTPROCESSINSTANCEID = B.SOURCEOBJECTID
    AND PROCESSDEFINITIONID = ?
	AND A.ROOTPROCESSINSTANCEID = B.ROOTPROCESSINSTANCEID
	and (STATEID = 6 OR STATEID = 3 OR STATEID = 4) AND ENDDATE <= ?)"""

        val processDefinitionId: Long = 6001594822724869891
        val dateBeforeWhichToPurge = 1582214307090

        val nbOfOtherProcessesCountBeforePurge = jdbcTemplate.queryForObject(rowsNotToDelete, arrayOf(processDefinitionId, dateBeforeWhichToPurge), Int::class.java)

        // One unfinished process (2 lines) + sub-process (2 lines)
        // + 11 lines of finished process instances but more recent than purge date 1582214307090:
        assertThat(nbOfOtherProcessesCountBeforePurge).isEqualTo(15)

        var nbOfArchProcessInstancesToDelete = jdbcTemplate.queryForObject(rowsToDelete, arrayOf(processDefinitionId, dateBeforeWhichToPurge), Int::class.java)
        assertThat(nbOfArchProcessInstancesToDelete).isEqualTo(44) // nb of corresponding lines to purge in data file 'arch_process_instance.csv'

        assertThat(jdbcTemplate.queryForObject("SELECT count(id) FROM arch_contract_data WHERE KIND = 'PROCESS'", Int::class.java)).isEqualTo(6)
        assertThat(jdbcTemplate.queryForObject("SELECT count(id) from arch_data_instance where CONTAINERTYPE = 'PROCESS_INSTANCE'", Int::class.java)).isEqualTo(23)
        assertThat(jdbcTemplate.queryForObject("SELECT count(id) from arch_document_mapping", Int::class.java)).isEqualTo(10)
        assertThat(jdbcTemplate.queryForObject("SELECT count(id) from arch_flownode_instance", Int::class.java)).isEqualTo(181)
        assertThat(jdbcTemplate.queryForObject("SELECT count(id) from arch_process_comment", Int::class.java)).isEqualTo(15)
        assertThat(jdbcTemplate.queryForObject("SELECT count(id) from arch_ref_biz_data_inst", Int::class.java)).isEqualTo(10)
        assertThat(jdbcTemplate.queryForObject("SELECT count(id) from arch_connector_instance WHERE CONTAINERTYPE = 'process'", Int::class.java)).isEqualTo(10)
        assertThat(jdbcTemplate.queryForObject("SELECT count(id) FROM arch_contract_data WHERE KIND = 'TASK'", Int::class.java)).isEqualTo(6)
        assertThat(jdbcTemplate.queryForObject("SELECT count(id) FROM arch_data_instance WHERE CONTAINERTYPE = 'ACTIVITY_INSTANCE'", Int::class.java)).isEqualTo(12)
        assertThat(jdbcTemplate.queryForObject("SELECT count(id) FROM arch_connector_instance WHERE CONTAINERTYPE = 'flowNode'", Int::class.java)).isEqualTo(11)


        // last finished process instance to purge at 1582214307090
        // 'All kinds of elements': 6001594822724869891
        // 'subProcess': 6776413672588351051
        // when:
        deleteOldProcessInstances.execute(processDefinitionId, dateBeforeWhichToPurge, 1L)

        // then:
        val nbOfOtherProcessesCountAfterPurge = jdbcTemplate.queryForObject(rowsNotToDelete, arrayOf(processDefinitionId, dateBeforeWhichToPurge), Int::class.java)
        assertThat(nbOfOtherProcessesCountAfterPurge).isEqualTo(nbOfOtherProcessesCountBeforePurge)

        nbOfArchProcessInstancesToDelete = jdbcTemplate.queryForObject(rowsToDelete, arrayOf(processDefinitionId, dateBeforeWhichToPurge), Int::class.java)
        assertThat(nbOfArchProcessInstancesToDelete).`as`("There should be not more lines to delete").isEqualTo(0)

        // 4 lines deleted, 2 remaining:
        val archContractDataForProcess = jdbcTemplate.queryForList("SELECT id FROM arch_contract_data WHERE KIND = 'PROCESS'", Long::class.java)
        assertThat(archContractDataForProcess.size).isEqualTo(2)
        assertThat(archContractDataForProcess).containsOnly(21L, 101L)
        // 16 lines deleted, 7 remaining:
        val archDataInstancesForProcess = jdbcTemplate.queryForList("SELECT id from arch_data_instance where CONTAINERTYPE = 'PROCESS_INSTANCE'", Long::class.java)
        assertThat(archDataInstancesForProcess.size).isEqualTo(7)
        assertThat(archDataInstancesForProcess).containsOnly(61L, 64L, 65L, 66L, 25001L, 25004L, 25005L)
        // 8 deleted, 2 remaining:
        val archDocumentMapping = jdbcTemplate.queryForList("SELECT id from arch_document_mapping", Long::class.java)
        assertThat(archDocumentMapping.size).isEqualTo(2)
        assertThat(archDocumentMapping).containsOnly(15L, 16L)
        // 50 arch flowNode instances remaining:
        assertThat(jdbcTemplate.queryForObject("SELECT count(id) from arch_flownode_instance", Int::class.java)).isEqualTo(50)
        // 12 deleted, 3 remaining:
        val archProcessComments = jdbcTemplate.queryForList("SELECT id from arch_process_comment", Long::class.java)
        assertThat(archProcessComments.size).isEqualTo(3)
        assertThat(archProcessComments).containsOnly(25L, 26L, 27L)
        val archRefBizDataInst = jdbcTemplate.queryForList("SELECT id from arch_ref_biz_data_inst", Long::class.java)
        assertThat(archRefBizDataInst.size).isEqualTo(2)
        assertThat(archRefBizDataInst).containsOnly(21L, 22L)
        val archConnectorInstForProcess = jdbcTemplate.queryForList("SELECT id FROM arch_connector_instance WHERE CONTAINERTYPE = 'process'", Long::class.java)
        assertThat(archConnectorInstForProcess.size).isEqualTo(2)
        assertThat(archConnectorInstForProcess).containsOnly(31L, 32L)
        // 4 lines deleted, 2 remaining:
        val archContractDataForTask = jdbcTemplate.queryForList("SELECT id FROM arch_contract_data WHERE KIND = 'TASK'", Long::class.java)
        assertThat(archContractDataForTask.size).isEqualTo(2)
        assertThat(archContractDataForTask).containsOnly(22L, 102L)
        // 8 lines deleted, 4 remaining:
        val archDataInstanceForTask = jdbcTemplate.queryForList("SELECT id FROM arch_data_instance WHERE CONTAINERTYPE = 'ACTIVITY_INSTANCE'", Long::class.java)
        assertThat(archDataInstanceForTask.size).isEqualTo(4)
        assertThat(archDataInstanceForTask).containsOnly(62L, 63L, 25_002L, 25_003L)
        // 8 lines deleted, 3 remaining:
        val archConnectorInstForTask = jdbcTemplate.queryForList("SELECT id FROM arch_connector_instance WHERE CONTAINERTYPE = 'flowNode'", Long::class.java)
        assertThat(archConnectorInstForTask.size).isEqualTo(3)
        assertThat(archConnectorInstForTask).containsOnly(29L, 30L, 101L)
    }

}