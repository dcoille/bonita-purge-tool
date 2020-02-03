package org.bonitasoft.engine.purge.tables

import org.jetbrains.exposed.sql.Table

/**
 * @author Emmanuel Duchastenier
 */
object ProcessDefinitionTable: Table("process_definition") {
    val id = long("processid")
    val name = varchar("name", 150)
    val version = varchar("version", 50)
}