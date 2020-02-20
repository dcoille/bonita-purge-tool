package org.bonitasoft.engine.purge

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.bonitasoft.engine.purge.tables.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.io.InputStreamReader

class ArchiveRepository {

    private val logger = LoggerFactory.getLogger(ArchiveRepository::class.java)

    fun instertArchContractData(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchContractDataTable.tenantId] = record.get(0).toLong()
        it[ArchContractDataTable.id] = record.get(1).toLong()
        it[ArchContractDataTable.kind] = record.get(2)
        it[ArchContractDataTable.scopeId] = record.get(3).toLong()
        it[ArchContractDataTable.name] = record.get(4)
        it[ArchContractDataTable.val_] = record.get(5)
        it[ArchContractDataTable.archiveDate] = record.get(6).toLong()
        it[ArchContractDataTable.sourceObjectId] = record.get(7).toLong()
    }

    fun insertArchProcessComment(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchProcessCommentTable.tenantId] = record.get(0).toLong()
        it[ArchProcessCommentTable.id] = record.get(1).toLong()
        it[ArchProcessCommentTable.processInstanceId] = record.get(3).toLong()
        it[ArchProcessCommentTable.archiveDate] = record.get(6).toLong()
    }

    fun insertArchDocumentMapping(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchDocumentMappingTable.tenantId] = record.get(0).toLong()
        it[ArchDocumentMappingTable.id] = record.get(1).toLong()
        it[ArchDocumentMappingTable.processInstanceId] = record.get(3).toLong()
        it[ArchDocumentMappingTable.documentid] = record.get(3).toLong()
        it[ArchDocumentMappingTable.name] = record.get(4)
        it[ArchDocumentMappingTable.archiveDate] = record.get(9).toLong()
    }

    private fun insertArchProcessInstance(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchProcessInstanceTable.tenantId] = record.get(0).toLong()
        it[ArchProcessInstanceTable.id] = record.get(1).toLong()
        it[ArchProcessInstanceTable.name] = record.get(2)
        it[ArchProcessInstanceTable.processDefinitionId] = record.get(3).toLong()
        it[ArchProcessInstanceTable.endDate] = record.get(8).toLong()
        it[ArchProcessInstanceTable.stateId] = record.get(10).toLong()
        it[ArchProcessInstanceTable.rootProcessInstanceId] = record.get(12).toLong()
        it[ArchProcessInstanceTable.sourceObjectId] = record.get(14).toLong()
    }

    fun insertArchFlowNodeInstance(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchFlowNodeinstanceTable.tenantId] = record.get(0).toLong()
        it[ArchFlowNodeinstanceTable.id] = record.get(1).toLong()
        it[ArchFlowNodeinstanceTable.flownodeDefinitionId] = record.get(2).toLong()
        it[ArchFlowNodeinstanceTable.archiveDate] = record.get(5).toLong()
        it[ArchFlowNodeinstanceTable.rootContainerId] = record.get(6).toLong()
    }

    fun insertArchConnectorInstance(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchConnectorInstanceTable.tenantId] = record.get(0).toLong()
        it[ArchConnectorInstanceTable.id] = record.get(1).toLong()
        it[ArchConnectorInstanceTable.containerId] = record.get(2).toLong()
        it[ArchConnectorInstanceTable.containerType] = record.get(3)
        it[ArchConnectorInstanceTable.version] = record.get(5)
        it[ArchConnectorInstanceTable.name] = record.get(6)
        it[ArchConnectorInstanceTable.archiveDate] = record.get(10).toLong()
    }

    fun insertArchRefBizDataInst(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchRefBizDataInstTable.tenantId] = record.get(0).toLong()
        it[ArchRefBizDataInstTable.id] = record.get(1).toLong()
        it[ArchRefBizDataInstTable.orig_proc_inst_id] = record.get(4).toLong()
    }

    fun insertArchMultiBizData(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchMultiBizDataTable.tenantId] = record.get(0).toLong()
        it[ArchMultiBizDataTable.id] = record.get(1).toLong()
        it[ArchMultiBizDataTable.idx] = record.get(2).toLong()
        it[ArchMultiBizDataTable.dataId] = record.get(3).toLong()
    }

    fun insertArchDataInstance(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchDataInstanceTable.tenantId] = record.get(0).toLong()
        it[ArchDataInstanceTable.id] = record.get(1).toLong()
        it[ArchDataInstanceTable.name] = record.get(2)
        it[ArchDataInstanceTable.containerId] = record.get(6).toLong()
        it[ArchDataInstanceTable.containerType] = record.get(7)
        it[ArchDataInstanceTable.archiveDate] = record.get(19).toLong()
    }

    private fun insertTenant(it: InsertStatement<Number>, record: CSVRecord) {
        it[Tenant.id] = record.get(0).toLong()
        it[Tenant.name] = record.get(7)
    }

    private fun insertProcessDefinition(it: InsertStatement<Number>, record: CSVRecord) {
        logger.debug("inserting process def ${record.get(2).toLong()}")
        it[ProcessDefinition.tenantId] = record.get(0).toLong()
        it[ProcessDefinition.id] = record.get(1).toLong()
        it[ProcessDefinition.processId] = record.get(2).toLong()
        it[ProcessDefinition.name] = record.get(3)
        it[ProcessDefinition.version] = record.get(4)
    }

    fun insert_data_before_purge() {
        logger.info("create tables using scripts")
        transaction {
            read("/data/tenant.csv").forEach { entry ->
                Tenant.insert {
                    insertTenant(it, entry)
                }
            }
        }
        transaction {
            read("/data/process_definition.csv").forEach { entry ->
                ProcessDefinition.insert {
                    insertProcessDefinition(it, entry)
                }
            }
        }
        transaction {
            read("/data/arch_process_instance.csv").forEach { entry ->
                ArchProcessInstanceTable.insert {
                    insertArchProcessInstance(it, entry)
                }
            }
        }
        transaction {
            read("/data/arch_contract_data.csv").forEach { entry ->
                ArchContractDataTable.insert {
                    instertArchContractData(it, entry)
                }
            }
        }
        transaction {
            read("/data/arch_process_comment.csv").forEach { entry ->
                ArchProcessCommentTable.insert {
                    insertArchProcessComment(it, entry)
                }
            }
        }
        transaction {
            read("/data/arch_document_mapping.csv").forEach { entry ->
                ArchDocumentMappingTable.insert {
                    insertArchDocumentMapping(it, entry)
                }
            }
        }
        transaction {
            read("/data/arch_flownode_instance.csv").forEach { entry ->
                ArchFlowNodeinstanceTable.insert {
                    insertArchFlowNodeInstance(it, entry)
                }
            }
        }
        transaction {
            read("/data/arch_connector_instance.csv").forEach { entry ->
                ArchConnectorInstanceTable.insert {
                    insertArchConnectorInstance(it, entry)
                }
            }

        }
        transaction {
            read("/data/arch_ref_biz_data.csv").forEach { entry ->
                ArchRefBizDataInstTable.insert {
                    insertArchRefBizDataInst(it, entry)
                }
            }
        }
        transaction {
            read("/data/arch_multi_biz_data.csv").forEach { entry ->
            ArchMultiBizDataTable.insert {
                    insertArchMultiBizData(it, entry)
                }
            }
        }
         transaction {
            read("/data/arch_data_instance.csv").forEach { entry ->
            ArchDataInstanceTable.insert {
                    insertArchDataInstance(it, entry)
                }
            }
        }
    }

    private fun read(fileName: String): List<CSVRecord> =
            CSVFormat.DEFAULT.parse(InputStreamReader(DeleteOldProcessInstancesIT::class.java.getResourceAsStream(fileName))).toList()

}