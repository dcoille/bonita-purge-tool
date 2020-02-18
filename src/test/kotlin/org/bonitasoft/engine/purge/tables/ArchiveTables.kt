package org.bonitasoft.engine.purge.tables

import org.jetbrains.exposed.sql.Table

object ArchProcessInstanceTable : Table("arch_process_instance") {
    val tenantId = long("tenantid").primaryKey()
    val id = long("id").primaryKey()
    val name = varchar("name", 75)
    val processDefinitionId = long("processdefinitionid")
    val endDate = long("enddate")
    val stateId = long("stateid")
    val rootProcessInstanceId = long("rootprocessinstanceid")
    val sourceObjectId = long("sourceobjectid")
}

object ArchContractDataTable : Table("arch_contract_data") {
    val tenantId = long("tenantid").primaryKey()
    val id = long("id").primaryKey()
    val kind = varchar("kind", 20)
    val scopeId = long("scopeid")
    val name = varchar("name", 50)
    val val_ = text("val").nullable()
    val archiveDate = long("archivedate")
    val sourceObjectId = long("sourceobjectid")
}

object ArchProcessCommentTable : Table("arch_process_comment") {
    val tenantId = long("tenantid").primaryKey()
    val id = long("id").primaryKey()
    val processInstanceId = long("processInstanceId")
    val archiveDate = long("archiveDate")
}

object ArchDocumentMappingTable : Table("arch_document_mapping") {
    val tenantId = long("tenantid").primaryKey()
    val id = long("id").primaryKey()
    val processInstanceId = long("processInstanceId")
    val documentid = long("documentid")
    val name = varchar("name", 50)
    val archiveDate = long("archiveDate")

}

object ArchFlowNodeinstanceTable : Table("arch_flownode_instance") {
    val tenantId = long("tenantid").primaryKey()
    val id = long("id").primaryKey()
    val archiveDate = long("archiveDate")
    val flownodeDefinitionId = long("flownodeDefinitionId")
}

object ArchConnectorInstanceTable : Table("arch_connector_instance") {
    val tenantId = long("tenantid").primaryKey()
    val id = long("id").primaryKey()
    val archiveDate = long("archiveDate")
    val name = varchar("name", 255)
    val version = varchar("version", 50)
}

object ArchRefBizDataInstTable : Table("arch_ref_biz_data_inst") {
    val tenantId = long("tenantid").primaryKey()
    val id = long("id").primaryKey()
}

object ArchMultiBizDataTable : Table("arch_multi_biz_data") {
    val tenantId = long("tenantid").primaryKey()
    val id = long("id").primaryKey()
    val dataId = long("data_id")

}

object ArchDataInstanceTable : Table("arch_data_instance") {
    val tenantId = long("tenantid").primaryKey()
    val id = long("id").primaryKey()
    val name = varchar("name", 50)
    val archiveDate = long("archiveDate")
}