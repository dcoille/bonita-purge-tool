package org.bonitasoft.engine.purge.tables

import org.jetbrains.exposed.sql.Table

/**
 * @author Emmanuel Duchastenier
 */
object ProcessDefinitionTable : Table("process_definition") {
    val id = long("processid")
    val name = varchar("name", 150)
    val version = varchar("version", 50)
}

object Tenant : Table("tenant") {
    val name = varchar("name", 50)
    val id = long("id")
}

object ArchProcessInstance : Table("arch_process_instance") {
    val definitionId = long("PROCESSDEFINITIONID")
}