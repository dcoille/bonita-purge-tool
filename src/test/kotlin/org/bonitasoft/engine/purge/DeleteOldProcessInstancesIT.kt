package org.bonitasoft.engine.purge

import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.BeforeTest

/**
 * @author Emmanuel Duchastenier
 */
@ExtendWith(MockKExtension::class)
internal class DeleteOldProcessInstancesIT {

    @Value("\${spring.datasource.driver-class-name:h2}")
    lateinit var driverClassName: String
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate
    @SpyK
    private var deleteOldProcessInstances = DeleteOldProcessInstances(true, "dummy url", jdbcTemplate)

    val logger = LoggerFactory.getLogger(DeleteOldProcessInstancesIT::class.java)


    @BeforeTest
    fun before() {
        transaction {
            logger.info("Drop tables")
            SchemaUtils.drop()
        }
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
    fun deleteOldProcessInstances_should_delete_the_records_of_a_process_before_a_timestamp_and_leave_records_of_other_processes_and_records_after_the_timestamp(){



        deleteOldProcessInstances.execute(12,12,12)


    }

}