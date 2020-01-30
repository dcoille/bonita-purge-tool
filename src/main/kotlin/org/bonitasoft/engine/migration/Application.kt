package org.bonitasoft.engine.migration

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    val context = SpringApplication.run(Application::class.java, *args)
    val bean = context.getBean(DeleteOldProcessInstances::class.java)
    bean.execute(args[0].toLong(), args[1].toLong()) // FIXME protect input parameters
    context.close()
}