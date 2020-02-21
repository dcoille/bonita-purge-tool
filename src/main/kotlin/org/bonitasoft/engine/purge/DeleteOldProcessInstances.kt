@file:Suppress("SqlResolve")

package org.bonitasoft.engine.purge

import org.bonitasoft.engine.purge.tables.ArchContractDataBackupTable
import org.bonitasoft.engine.purge.tables.ArchProcessInstance
import org.bonitasoft.engine.purge.tables.ProcessDefinition
import org.bonitasoft.engine.purge.tables.Tenant
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

@Service
class DeleteOldProcessInstances(
        @Value("\${org.bonitasoft.engine.migration.skip_confirmation:true}") private val skipConfirmation: Boolean,
        @Value("\${spring.datasource.url:#{null}}") private val databaseUrl: String?,
        private val jdbcTemplate: JdbcTemplate) {

    private val logger = LoggerFactory.getLogger(DeleteOldProcessInstances::class.java)

    fun execute(processDefinitionId: Long, date: Long, tenantId: Long?) {
        val validTenantId = checkTenantIdValidity(tenantId)

        logger.info("Database URL is $databaseUrl")
        logger.info("Tenant id used is $validTenantId")
        logger.info("All settings can be changed in application.properties file")

        val processDefinition = getProcessDefinition(processDefinitionId)
        when {
            processDefinition.isEmpty() -> {
                logger.error("No process definition exists for id $processDefinitionId. Exiting.")
                quitWithCode(1)
            }
            countArchivedProcessInstances(processDefinitionId) == 0 -> {
                logger.warn("""
                    |No finished process instance exists for process '${processDefinition[0].first}' in version '${processDefinition[0].second}'.
                    |Continuing will purge all archived orphan elements that may remain from a previous interrupted purge execution.
                    """.trimMargin())
            }
            else -> {
                logger.info("Will purge all archived process instances and their elements for process " +
                        "'${processDefinition[0].first}' in version '${processDefinition[0].second}'" +
                        " that are finished since at least ${Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDateTime()}")
            }
        }
        if (!skipConfirmation) {
            println("Start the purge using the above parameters? [y/N]")
            val readLine = readLine()
            if ("Y" != readLine?.toUpperCase()) {
                logger.warn("Purge cancelled")
                quitWithCode(2)
            }
        }
        logger.info("Starting archive process instance purge....")
        doExecutePurge(processDefinitionId, date, validTenantId)
        purgeArchContractDataTableIfExists(validTenantId)
        logPurgeFinishedAndWarn()
    }

    internal fun doExecutePurge(processDefinitionId: Long, date: Long, validTenantId: Long) {
        val nbRows = jdbcTemplate.update("""
    DELETE FROM ARCH_PROCESS_INSTANCE A WHERE exists (
    SELECT rootprocessinstanceid
    FROM ARCH_PROCESS_INSTANCE B
    WHERE B.ROOTPROCESSINSTANCEID = B.SOURCEOBJECTID
    AND A.ROOTPROCESSINSTANCEID = B.ROOTPROCESSINSTANCEID
    AND PROCESSDEFINITIONID = ?
    and (STATEID = 6 OR STATEID = 3 OR STATEID = 4)
    AND ENDDATE <= ?) AND tenantId = ?""", processDefinitionId, date, validTenantId)
        logger.info("Deleted $nbRows rows from table ARCH_PROCESS_INSTANCE...")

        executeSQLScript("/delete_scenario.sql", validTenantId)
    }

    internal fun purgeArchContractDataTableIfExists(validTenantId: Long) {
        val archContractDataBackupTableExists = transaction {
            ArchContractDataBackupTable.exists()
        }
        if (archContractDataBackupTableExists) {
            logger.info("Detected presence of table ARCH_CONTRACT_DATA_BACKUP. Purging it as well.")
            executeSQLScript("/optional_backup_table_purge.sql", validTenantId)
        }
    }

    private fun executeSQLScript(sqlScriptFile: String, validTenantId: Long) {
        val statements: MutableList<String> = mutableListOf()
        ScriptUtils.splitSqlScript(this::class.java.getResource(sqlScriptFile).readText(Charsets.UTF_8), ";", statements)
        statements.forEach { statement ->
            run {
                logger.debug("Executing SQL: $statement")
                var nbRowsDeleted = 0
                val executionTime = measureTimeMillis {
                    nbRowsDeleted = jdbcTemplate.update(statement, validTenantId, validTenantId)
                }
                val startIndex = statement.indexOf("FROM ") + 5 // FROM must be written in capital letters !
                val tableName = statement.substring(startIndex, statement.indexOf(" ", startIndex))
                logger.info("Deleted $nbRowsDeleted rows from table $tableName in $executionTime ms")
            }
        }
    }

    internal fun checkTenantIdValidity(tenantId: Long?): Long {
        val tenants = getAllTenants()
        if (tenants.isEmpty()) { // 0 tenant:
            logger.error("No tenant exists. Platform invalid")
            quitWithCode(1)
        }
        return if (tenantId == null) {
            if (tenants.size > 1) {
                logger.error("Multiple tenants exist ${tenants.entries}. Please specify tenant ID as 3rd parameter")
                quitWithCode(1)
            }
            else tenants.keys.first()
        } else {
            if (!tenants.containsKey(tenantId)) {
                logger.error("Tenant with ID $tenantId does not exist. Available tenants are ${tenants.entries}")
                quitWithCode(1)
            } else {
                tenantId
            }
        }
    }

    internal fun getAllTenants() = transaction {
        Tenant.slice(Tenant.id, Tenant.name)
                .selectAll()
                .map { it[Tenant.id] to it[Tenant.name] }.toMap()
    }

    internal fun getProcessDefinition(processDefinitionId: Long): List<Pair<String, String>> = transaction {
        ProcessDefinition
                .select {
                    ProcessDefinition.processId eq processDefinitionId
                }
                .map { it[ProcessDefinition.name] to it[ProcessDefinition.version] }
    }

    internal fun countArchivedProcessInstances(processDefinitionId: Long): Int = transaction {
        ArchProcessInstance
                .select {
                    ArchProcessInstance.definitionId eq processDefinitionId
                }
                .count()
    }

    private fun logPurgeFinishedAndWarn() {
        logger.info("Archive process instance purge completed.")
        logger.info("Some of the deleted elements may still appear in Bonita Portal for a short while.")
        logger.info("If you try to access them you will get a not found error. This is the expected behaviour.")
    }

    internal fun quitWithCode(i: Int): Nothing = exitProcess(i)

}
