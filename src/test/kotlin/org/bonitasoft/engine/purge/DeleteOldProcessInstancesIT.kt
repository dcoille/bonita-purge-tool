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
@SpringBootTest
class DeleteOldProcessInstancesIT {

    @Autowired
    lateinit var deleteOldProcessInstances: DeleteOldProcessInstances

    @Value("\${spring.datasource.driver-class-name:h2}")
    lateinit var driverClassName: String

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private var archiveRepository = ArchiveRepository()

    private val logger: Logger = LoggerFactory.getLogger(DeleteOldProcessInstancesIT::class.java)


    @BeforeTest
    fun before() {
        transaction {
            logger.info("Drop test tables")
            SchemaUtils.drop()
        }
        logger.debug("jdbcTemplate connection url: ${jdbcTemplate.dataSource?.connection?.metaData?.url}")
        transaction {
            when {
                driverClassName.contains("postgresql") -> {
                    createTablesFromScript("/postgres.sql")
                }
                driverClassName.contains("oracle") -> {
                    createTablesFromScript("/oracle.sql")
                }
                driverClassName.contains("mysql") -> {
                    createTablesFromScript("/mysql.sql")
                }
                driverClassName.contains("sqlserver") -> {
                    createTablesFromScript("/sqlserver.sql")
                }
            }
            archiveRepository.insert_data_before_purge()
        }
    }

    @Test
    fun `should delete the records of a process before a timestamp and leave records of other processes and records after the timestamp`() {

        // given:
        val rowsNotToDelete = """
select count(id) FROM ARCH_PROCESS_INSTANCE A WHERE not exists (
	SELECT rootprocessinstanceid
	FROM ARCH_PROCESS_INSTANCE B
	WHERE B.ROOTPROCESSINSTANCEID = B.SOURCEOBJECTID
    AND PROCESSDEFINITIONID = ?
	AND A.ROOTPROCESSINSTANCEID = B.ROOTPROCESSINSTANCEID
	and (STATEID = 6 OR STATEID = 3 OR STATEID = 4) AND ENDDATE <= ?)"""

        val rowsToDelete = """
select count(id) FROM ARCH_PROCESS_INSTANCE A WHERE exists (
	SELECT rootprocessinstanceid
	FROM ARCH_PROCESS_INSTANCE B
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
    }

    private fun createTablesFromScript(fileName: String) {
        logger.info("create tables using script $fileName")
        val content = DeleteOldProcessInstancesIT::class.java.getResource(fileName).readText()
        content.split(";").filter { !it.isBlank() }.forEach {
            jdbcTemplate.execute(it.trim())
        }
    }

}