package org.bonitasoft.engine.purge

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

@SpringBootApplication
open class Application

val logger = LoggerFactory.getLogger(Application::class.java)

fun main(args: Array<String>) {
    if (!UniqueInstance.createSemaphore()) {
        exitProcess(-1)
    }
    val context = SpringApplication.run(Application::class.java, *args)
    val processInstancePurge = context.getBean(DeleteOldProcessInstances::class.java)
    // tenantId, parameter number 3, is optional:
    val tenantId = if (args.size > 2) getLong(args[2]) else null
    if (args.size < 2) {
        usage()
    }
    val executionTime = measureTimeMillis {
        processInstancePurge.execute(getLong(args[0]), getLong(args[1]), tenantId)
    }
    logger.info("Execution completed in $executionTime ms")
    context.close()
    UniqueInstance.releaseSemaphore()
    exitProcess(0)
}

private fun usage(): Nothing {
    logger.error("""Invalid number/format of parameters.
Usage: bonita-purge-tool(.bat) <PROCESS_DEFINITION_ID> <OLDEST_DATE_TIMESTAMP> [<TENANT_ID>]
       This command will delete all archived process instances belonging to the Process
       identified by PROCESS_DEFINITION_ID, that are finished since at least OLDEST_DATE_TIMESTAMP (in milliseconds since Epoch).
       An optional TENANT_ID parameter can be given if the platform uses multiple tenants to specify on which tenant should the process instances be deleted.
       If multi-tenancy is used and the TENANT_ID is not set, an error is issued and the program stops.
""")
    exitProcess(-1)
}

private fun getLong(arg: String) = try {
    arg.toLong()
} catch (e: NumberFormatException) {
    logger.error("Invalid input parameter: ${e.message}")
    usage()
}