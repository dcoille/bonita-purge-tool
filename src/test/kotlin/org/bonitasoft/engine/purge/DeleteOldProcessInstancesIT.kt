package org.bonitasoft.engine.purge

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
        logger.debug("jdbcTemplate connection url: ${jdbcTemplate.dataSource.connection.metaData.url}")
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

    private fun createTablesFromScript(fileName: String) {
        logger.info("create tables using script $fileName")
        val content = DeleteOldProcessInstancesIT::class.java.getResource(fileName).readText()
        content.split(";").filter { !it.isBlank() }.forEach {
            jdbcTemplate.execute(it.trim())
        }
    }

    @Test
    fun `should delete the records of a process before a timestamp and leave records of other processes and records after the timestamp`() {
        deleteOldProcessInstances.execute(8663749350673053802, 1000000 /*old date*/, null)
//        transaction {
//            Arch
//        }
    }

}