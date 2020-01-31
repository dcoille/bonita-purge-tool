package org.bonitasoft.engine.migration

import org.bonitasoft.engine.migration.tables.ProcessDefinitionTable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import kotlin.system.exitProcess

@Service
class DeleteOldProcessInstances(
        @Value("\${org.bonitasoft.engine.migration.skip_confirmation:true}") val skipConfirmation: Boolean,
        @Value("\${spring.datasource.url:#{null}}") val databaseUrl: String?,
        private val jdbcTemplate: JdbcTemplate) {

    private val logger = LoggerFactory.getLogger(DeleteOldProcessInstances::class.java)

    fun execute(processDefinitionId: Long, date: Long, tenantId: Long = 1L) {
        transaction {
            ProcessDefinitionTable
                    .select {
                        ProcessDefinitionTable.id eq processDefinitionId
                    }
                    .forEach {
                        logger.info("Will purge all archived process instances and their elements for process " +
                                "'${it[ProcessDefinitionTable.name]}' in version '${it[ProcessDefinitionTable.version]}'" +
                                " that are finished since at least ${Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDateTime()}")
                    }
        }
        logger.info("Database is $databaseUrl")
        logger.info("Tenant id is $tenantId")
        logger.info("All settings can be changed in application.properties file")
        if (!skipConfirmation) {
            println("Start the purge using the above parameters? [y/N]")
            val readLine = readLine()
            if ("Y" != readLine?.toUpperCase()) {
                println("Purge cancelled")
                exitProcess(1)
            }
        }
        logger.info("Starting archive process instance purge....")
        val nbRows = jdbcTemplate.update("""
DELETE FROM ARCH_PROCESS_INSTANCE A WHERE exists (
SELECT rootprocessinstanceid
FROM ARCH_PROCESS_INSTANCE B
WHERE PROCESSDEFINITIONID = ? AND ENDDATE > ?
AND A.ROOTPROCESSINSTANCEID = B.ROOTPROCESSINSTANCEID) AND tenantId = ?""", processDefinitionId, date, tenantId)
        logger.info("Deleted $nbRows lines from table ARCH_PROCESS_INSTANCE...")

        val statements: MutableList<String> = mutableListOf()
        ScriptUtils.splitSqlScript(this::class.java.getResource("/delete_scenario.sql").readText(Charsets.UTF_8), ";", statements)
        statements.forEach { statement ->
            run {
                logger.info("Executing SQL: $statement")
                logger.info("${jdbcTemplate.update(statement, tenantId, tenantId)} rows deleted")
            }
        }

    }
}
