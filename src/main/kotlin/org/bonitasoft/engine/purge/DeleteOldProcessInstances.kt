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
import java.net.ConnectException
import java.time.Instant
import java.time.ZoneId
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

@Service
class DeleteOldProcessInstances(
        @Value("\${org.bonitasoft.engine.purge.skip_confirmation:true}") private val skipConfirmation: Boolean,
        @Value("\${spring.datasource.url:#{null}}") private val databaseUrl: String?,
        @Value("\${spring.datasource.driver-class-name:org.postgresql.Driver}") private val driverClassName: String,
        @Value("\${org.bonitasoft.engine.purge.mysql.bulk.size}") private val bulkSize: Int,
        private val jdbcTemplate: JdbcTemplate) {

    private val logger = LoggerFactory.getLogger(DeleteOldProcessInstances::class.java)

    private lateinit var dbVendor: String

    fun execute(processDefinitionId: Long, date: Long, tenantId: Long?) {
        dbVendor = getDbVendor(driverClassName)
        val validTenantId = checkTenantIdValidity(tenantId)

        logger.info("Database URL is $databaseUrl")
        logger.info("Tenant id used is $validTenantId")
        logger.info("All settings can be changed in application.properties file")

        val processDefinition = getProcessDefinition(processDefinitionId)
        when {
            processDefinition.isEmpty() -> {
                logger.error("No process definition exists for id $processDefinitionId.")
                logger.info("Existing process definitions are:\n${getAllProcessDefinitions().joinToString("\n")}")
                quitWithCode(1)
            }
            countArchivedProcessInstances(processDefinitionId) == 0 -> {
                logger.warn("""
                    |No finished process instance exists for process '${processDefinition[0].first}' in version '${processDefinition[0].second}'
                    |before ${toLocalDateTime(date)}
                    |Continuing will purge all archived orphan elements that may remain from a previous interrupted purge execution.
                    """.trimMargin())
            }
            else -> {
                logger.info("Will purge all archived process instances and their elements for process " +
                        "'${processDefinition[0].first}' in version '${processDefinition[0].second}'" +
                        " that are finished since at least ${toLocalDateTime(date)}")
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
        logger.info("Starting archive process instance purge...")
        doExecutePurge(processDefinitionId, date, validTenantId)

        logPurgeFinishedAndWarn()
    }

    private fun toLocalDateTime(date: Long) =
            Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDateTime()

    internal fun doExecutePurge(processDefinitionId: Long, date: Long, validTenantId: Long) {

        executeSQLScript("/$dbVendor/1_pre_purge.sql")

        val statements = getSQLStatements("/$dbVendor/2_delete_arch_process_instance.sql")
        logger.debug("Executing SQL: ${statements[0]}")
        var nbRows = 0
        val executionTime = measureTimeMillis {
            nbRows = jdbcTemplate.update(statements[0], processDefinitionId, date, validTenantId)
        }
        logger.info("Deleted $nbRows rows from table ARCH_PROCESS_INSTANCE in $executionTime ms")

        executeSQLScript("/$dbVendor/3_delete_orphans.sql", validTenantId)
        purgeArchContractDataTableIfExists(validTenantId)
        executeSQLScript("/$dbVendor/5_post_purge.sql")
    }

    internal fun purgeArchContractDataTableIfExists(validTenantId: Long) {
        val archContractDataBackupTableExists = transaction {
            ArchContractDataBackupTable.exists()
        }
        if (archContractDataBackupTableExists) {
            logger.info("Detected presence of table ARCH_CONTRACT_DATA_BACKUP. Purging it as well.")
            executeSQLScript("/$dbVendor/4_optional_delete_orphans.sql", validTenantId)
        }
    }

    private fun executeSQLScript(sqlScriptFile: String) {
        val statements = getSQLStatements(sqlScriptFile)
        statements.forEach { statement ->
            run {
                logger.debug("Executing SQL: $statement")
                val executionTime = measureTimeMillis {
                    jdbcTemplate.update(statement)
                }
                logger.debug("SQL command executed in $executionTime ms")
            }
        }
    }

    private fun executeSQLScript(sqlScriptFile: String, validTenantId: Long) {
        val statements = getSQLStatements(sqlScriptFile)
        statements.forEach { statement ->
            val startIndex = statement.indexOf("FROM ") + 5 // FROM must be written in capital letters !
            val tableName = statement.substring(startIndex, statement.indexOf(" ", startIndex))
            run {
                logger.debug("Executing SQL: $statement")
                var nbRowsDeleted = 0
                do {
                    val executionTime = measureTimeMillis {
                        nbRowsDeleted = if (dbVendor == "mysql") {
                            jdbcTemplate.update(statement, validTenantId, validTenantId, bulkSize)
                        } else {
                            jdbcTemplate.update(statement, validTenantId, validTenantId)
                        }
                    }
                    logger.info("Deleted $nbRowsDeleted rows from table $tableName in $executionTime ms")
                } while (nbRowsDeleted == bulkSize)
            }
        }
    }

    private fun getSQLStatements(script: String): MutableList<String> {
        val statements: MutableList<String> = mutableListOf()
        ScriptUtils.splitSqlScript(this::class.java.getResource(script).readText(Charsets.UTF_8), ";", statements)
        return statements
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
            } else tenants.keys.first()
        } else {
            if (!tenants.containsKey(tenantId)) {
                logger.error("Tenant with ID $tenantId does not exist. Available tenants are ${tenants.entries}")
                quitWithCode(1)
            } else {
                tenantId
            }
        }
    }

    internal fun getAllTenants(): Map<Long, String> = transaction {
        handleException {
            Tenant.slice(Tenant.id, Tenant.name)
                    .selectAll()
                    .map { it[Tenant.id] to it[Tenant.name] }.toMap()
        }
    }

    internal fun getProcessDefinition(processDefinitionId: Long): List<Pair<String, String>> = transaction {
        ProcessDefinition
                .select {
                    ProcessDefinition.processId eq processDefinitionId
                }
                .map { it[ProcessDefinition.name] to it[ProcessDefinition.version] }
    }

    internal fun getAllProcessDefinitions(): MutableList<String> {
        val definitions = mutableListOf<String>()
        transaction {
            for (d in ProcessDefinition.selectAll()) {
                definitions.add("${d[ProcessDefinition.processId]} - ${d[ProcessDefinition.name]} (${d[ProcessDefinition.version]})")
            }
        }
        return definitions
    }

    internal fun countArchivedProcessInstances(processDefinitionId: Long): Int = transaction {
        ArchProcessInstance
                .select { ArchProcessInstance.definitionId eq processDefinitionId }.count()
    }

    private fun logPurgeFinishedAndWarn() {
        logger.info("Archive process instance purge completed.")
        logger.info("Some of the deleted elements may still appear in Bonita Portal for a short while.")
        logger.info("If you try to access them you will get a not found error. This is the expected behaviour.")
    }

    private fun <T> handleException(block: () -> T): T = try {
        block()
    } catch (e: Exception) {
        if (e.cause is ConnectException) {
            logger.error("Fail to connect to database: ${e.message}")
        }
        exitProcess(-1)
    }

    internal fun quitWithCode(i: Int): Nothing = exitProcess(i)
}

fun getDbVendor(driverClassName: String) =
        when {
            driverClassName.contains("postgresql") -> "postgres"
            driverClassName.contains("oracle") -> "oracle"
            driverClassName.contains("mysql") -> "mysql"
            driverClassName.contains("sqlserver") -> "sqlserver"
            else -> throw RuntimeException("Cannot determine database vendor from driver $driverClassName")
        }
