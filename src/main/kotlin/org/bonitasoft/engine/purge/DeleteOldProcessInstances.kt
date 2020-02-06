@file:Suppress("SqlResolve")

package org.bonitasoft.engine.purge

import org.bonitasoft.engine.purge.tables.ArchProcessInstance
import org.bonitasoft.engine.purge.tables.ProcessDefinitionTable
import org.bonitasoft.engine.purge.tables.Tenant
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
        @Value("\${org.bonitasoft.engine.migration.skip_confirmation:true}") val skipConfirmation: Boolean,
        @Value("\${spring.datasource.url:#{null}}") val databaseUrl: String?,
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
                logger.info("No finished process instance exists for process " +
                        "'${processDefinition[0].first}' in version '${processDefinition[0].second}'")
                quitWithCode(1)
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
        val nbRows = jdbcTemplate.update("""
DELETE FROM ARCH_PROCESS_INSTANCE A WHERE exists (
SELECT rootprocessinstanceid
FROM ARCH_PROCESS_INSTANCE B
WHERE B.ROOTPROCESSINSTANCEID = B.SOURCEOBJECTID
AND A.ROOTPROCESSINSTANCEID = B.ROOTPROCESSINSTANCEID
AND PROCESSDEFINITIONID = ? AND ENDDATE < ?) AND tenantId = ?""", processDefinitionId, date, validTenantId)
        logger.info("Deleted $nbRows lines from table ARCH_PROCESS_INSTANCE...")

        val statements: MutableList<String> = mutableListOf()
        ScriptUtils.splitSqlScript(this::class.java.getResource("/delete_scenario.sql").readText(Charsets.UTF_8), ";", statements)
        statements.forEach { statement ->
            run {
                logger.info("Executing SQL: $statement")
                var nbRowsDeleted = 0
                val executionTime = measureTimeMillis {
                    nbRowsDeleted = jdbcTemplate.update(statement, validTenantId, validTenantId)
                }
                logger.info("$nbRowsDeleted rows deleted in $executionTime ms")
            }
        }
    }

    fun checkTenantIdValidity(tenantId: Long?): Long {
        val tenants = getAllTenants()
        if (tenantId == null) {
            when {
                tenants.size > 1 -> {
                    logger.error("Multiple tenants exist ${tenants.entries}. Please specify tenant ID as 3rd parameter")
                    quitWithCode(1)
                }
                tenants.isEmpty() -> { // 0 tenants:
                    logger.error("No tenant exists. Platform invalid")
                    quitWithCode(1)
                }
                else -> return tenants.keys.first()
            }
        } else {
            if (!tenants.containsKey(tenantId)) {
                logger.error("Tenant with ID $tenantId does not exist. Available tenants are ${tenants.entries}")
                quitWithCode(1)
            } else {
                return tenantId
            }
        }
    }

    fun quitWithCode(i: Int): Nothing = exitProcess(i)

    fun getAllTenants() =
            Tenant.slice(Tenant.id, Tenant.name)
                    .selectAll()
                    .map { it[Tenant.id] to it[Tenant.name] }.toMap()

    fun getProcessDefinition(processDefinitionId: Long): List<Pair<String, String>> = transaction {
        ProcessDefinitionTable
                .select {
                    ProcessDefinitionTable.id eq processDefinitionId
                }
                .map { it[ProcessDefinitionTable.name] to it[ProcessDefinitionTable.version] }
    }

    fun countArchivedProcessInstances(processDefinitionId: Long): Int = transaction {
        ArchProcessInstance
                .select {
                    ArchProcessInstance.definitionId eq processDefinitionId
                }
                .count()
    }

}
