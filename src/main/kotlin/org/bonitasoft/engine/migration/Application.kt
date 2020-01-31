package org.bonitasoft.engine.migration

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    val context = SpringApplication.run(Application::class.java, *args)
    val bean = context.getBean(DeleteOldProcessInstances::class.java)
    // tenantId, parameter number 3, is optional:
    val tenantId = if (args.size > 2) args[2].toLong() else 1L
    bean.execute(args[0].toLong(), args[1].toLong(), tenantId) // FIXME protect input parameters
    context.close()
}