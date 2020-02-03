package org.bonitasoft.engine.purge

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import kotlin.system.exitProcess

@SpringBootApplication
open class Application

private val logger = LoggerFactory.getLogger(Application::class.java)

fun main(args: Array<String>) {

    val context = SpringApplication.run(Application::class.java, *args)
    val bean = context.getBean(DeleteOldProcessInstances::class.java)
    // tenantId, parameter number 3, is optional:
    val tenantId = if (args.size > 2) args[2].toLong() else 1L
    if (args.size < 2) {
        usage()
        exitProcess(-1)
    }
    bean.execute(args[0].toLong(), args[1].toLong(), tenantId) // FIXME check tenantID set if platform is multi-tenant
    context.close()
    exitProcess(0)
}

private fun usage() {
    logger.error("""Invalid number of parameters
Usage: bonita-purge-tool(.bat) <PROCESS_DEFINITION_ID> <OLDEST_DATE_TIMESTAMP> [<TENAND_ID>]
       This command will delete all archived process instances belonging to the Process
       identified by PROCESS_DEFINITION_ID, that are finished since at least OLDEST_DATE_TIMESTAMP.
       An optional TENAND_ID parameter can be given if the platform uses multiple tenants to specify on which tenant should the process instances be deleted.
       If multi-tenancy is used and the TENAND_ID is not set, an error is issued and the program stops.
""")

}