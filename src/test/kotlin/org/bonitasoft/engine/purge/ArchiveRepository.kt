package org.bonitasoft.engine.purge

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.bonitasoft.engine.purge.tables.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.slf4j.LoggerFactory
import java.io.InputStreamReader

class ArchiveRepository {

    val logger = LoggerFactory.getLogger(ArchiveRepository::class.java)

    fun createArchContractData(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchContractDataTable.tenantId] = record.get(0).toLong()
        it[ArchContractDataTable.id] = record.get(1).toLong()
        it[ArchContractDataTable.kind] = record.get(2)
        it[ArchContractDataTable.scopeId] = record.get(3).toLong()
        it[ArchContractDataTable.name] = record.get(4)
        it[ArchContractDataTable.val_] = record.get(5)
        it[ArchContractDataTable.archiveDate] = record.get(6).toLong()
        it[ArchContractDataTable.sourceObjectId] = record.get(7).toLong()
    }

    fun createArchProcessComment(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchProcessCommentTable.tenantId] = record.get(0).toLong()
        it[ArchProcessCommentTable.id] = record.get(1).toLong()
        it[ArchProcessCommentTable.processInstanceId] = record.get(3).toLong()
        it[ArchProcessCommentTable.archiveDate] = record.get(6).toLong()
    }

    fun createArchDocumentMappingTable(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchDocumentMappingTable.tenantId] = record.get(0).toLong()
        it[ArchDocumentMappingTable.id] = record.get(1).toLong()
        it[ArchDocumentMappingTable.processInstanceId] = record.get(3).toLong()
        it[ArchDocumentMappingTable.documentid] = record.get(3).toLong()
        it[ArchDocumentMappingTable.name] = record.get(4)
        it[ArchDocumentMappingTable.archiveDate] = record.get(9).toLong()
    }

     fun createArchProcessInstanceTable(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchProcessInstanceTable.tenantId] = record.get(0).toLong()
        it[ArchProcessInstanceTable.id] = record.get(1).toLong()
        it[ArchProcessInstanceTable.name] = record.get(2)
        it[ArchProcessInstanceTable.processDefinitionId] = record.get(3).toLong()
        it[ArchProcessInstanceTable.archiveDate] = record.get(9).toLong()
    }

    fun createArchFlowNodeinstanceTable(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchFlowNodeinstanceTable.tenantId] = record.get(0).toLong()
        it[ArchFlowNodeinstanceTable.id] = record.get(1).toLong()
        it[ArchFlowNodeinstanceTable.flownodeDefinitionId] = record.get(2).toLong()
        it[ArchFlowNodeinstanceTable.archiveDate] = record.get(5).toLong()
    }

    fun createArchConnectorInstanceTable(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchConnectorInstanceTable.tenantId] = record.get(0).toLong()
        it[ArchConnectorInstanceTable.id] = record.get(1).toLong()
        it[ArchConnectorInstanceTable.version] = record.get(5)
        it[ArchConnectorInstanceTable.name] = record.get(6)
        it[ArchConnectorInstanceTable.archiveDate] = record.get(10).toLong()
    }

    fun createArchRefBizDataInstTable(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchRefBizDataInstTable.tenantId] = record.get(0).toLong()
        it[ArchRefBizDataInstTable.id] = record.get(1).toLong()
    }

    fun createArchMultiBizDataTable(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchMultiBizDataTable.tenantId] = record.get(0).toLong()
        it[ArchMultiBizDataTable.id] = record.get(1).toLong()
        it[ArchMultiBizDataTable.dataId] = record.get(3).toLong()
    }

    fun createArchDataInstanceTable(it: InsertStatement<Number>, record: CSVRecord) {
        it[ArchDataInstanceTable.tenantId] = record.get(0).toLong()
        it[ArchDataInstanceTable.id] = record.get(1).toLong()
        it[ArchDataInstanceTable.name] = record.get(2)
        it[ArchDataInstanceTable.archiveDate] = record.get(19).toLong()
    }

    fun insert_data_before_purge() {
        logger.info("create tables using scripts")
        read("/arch_process_instance.csv").forEach { entry ->
            ArchProcessInstanceTable.insert {
                createArchProcessInstanceTable(it, entry)
            }
        }
    }

    private fun read(fileName: String): List<CSVRecord> {
        var content = DeleteOldProcessInstancesIT::class.java.getResourceAsStream("/$fileName")
        return CSVFormat.DEFAULT.parse(InputStreamReader(content)).toList()
    }

}