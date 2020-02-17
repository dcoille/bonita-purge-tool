CREATE TABLE arch_contract_data (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(20) NOT NULL,
  scopeId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  val NVARCHAR(MAX),
  archiveDate NUMERIC(19, 0) NOT NULL,
  sourceObjectId NUMERIC(19, 0) NOT NULL
)
GO
ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id, scopeId)
GO
ALTER TABLE arch_contract_data ADD CONSTRAINT uc_acd_scope_name UNIQUE (kind, scopeId, name, tenantid)
GO
CREATE INDEX idx_acd_scope_name ON arch_contract_data (kind, scopeId, name, tenantid)
GO

CREATE TABLE arch_process_comment(
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0),
  processInstanceId NUMERIC(19, 0) NOT NULL,
  postDate NUMERIC(19, 0) NOT NULL,
  content NVARCHAR(512) NOT NULL,
  archiveDate NUMERIC(19, 0) NOT NULL,
  sourceObjectId NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx1_arch_process_comment on arch_process_comment (sourceobjectid, tenantid)
GO
CREATE INDEX idx2_arch_process_comment on arch_process_comment (processInstanceId, archivedate, tenantid)
GO

CREATE TABLE arch_document_mapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  sourceObjectId NUMERIC(19, 0),
  processinstanceid NUMERIC(19, 0) NOT NULL,
  documentid NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  description NVARCHAR(MAX),
  version NVARCHAR(50) NOT NULL,
  index_ INT NOT NULL,
  archiveDate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, ID)
)
GO
CREATE INDEX idx_a_doc_mp_pr_id ON arch_document_mapping (processinstanceid, tenantid)
GO

CREATE TABLE arch_process_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(75) NOT NULL,
  processDefinitionId NUMERIC(19, 0) NOT NULL,
  description NVARCHAR(255),
  startDate NUMERIC(19, 0) NOT NULL,
  startedBy NUMERIC(19, 0) NULL,
  startedBySubstitute NUMERIC(19, 0) NOT NULL,
  endDate NUMERIC(19, 0) NOT NULL,
  archiveDate NUMERIC(19, 0) NOT NULL,
  stateId INT NOT NULL,
  lastUpdate NUMERIC(19, 0) NOT NULL,
  rootProcessInstanceId NUMERIC(19, 0),
  callerId NUMERIC(19, 0),
  sourceObjectId NUMERIC(19, 0) NOT NULL,
  stringIndex1 NVARCHAR(255),
  stringIndex2 NVARCHAR(255),
  stringIndex3 NVARCHAR(255),
  stringIndex4 NVARCHAR(255),
  stringIndex5 NVARCHAR(255),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx1_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, rootProcessInstanceId, callerId)
GO
CREATE INDEX idx2_arch_process_instance ON arch_process_instance (tenantId, processDefinitionId, archiveDate)
GO
CREATE INDEX idx3_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, callerId, stateId)
GO

CREATE TABLE arch_flownode_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  flownodeDefinitionId NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(25) NOT NULL,
  sourceObjectId NUMERIC(19, 0),
  archiveDate NUMERIC(19, 0) NOT NULL,
  rootContainerId NUMERIC(19, 0) NOT NULL,
  parentContainerId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  displayName NVARCHAR(255),
  displayDescription NVARCHAR(255),
  stateId INT NOT NULL,
  stateName NVARCHAR(50),
  terminal BIT NOT NULL,
  stable BIT ,
  actorId NUMERIC(19, 0) NULL,
  assigneeId NUMERIC(19, 0) DEFAULT 0 NOT NULL,
  reachedStateDate NUMERIC(19, 0),
  lastUpdateDate NUMERIC(19, 0),
  expectedEndDate NUMERIC(19, 0),
  claimedDate NUMERIC(19, 0),
  priority TINYINT,
  gatewayType NVARCHAR(50),
  hitBys NVARCHAR(255),
  logicalGroup1 NUMERIC(19, 0) NOT NULL,
  logicalGroup2 NUMERIC(19, 0) NOT NULL,
  logicalGroup3 NUMERIC(19, 0),
  logicalGroup4 NUMERIC(19, 0) NOT NULL,
  loop_counter INT,
  loop_max INT,
  loopCardinality INT,
  loopDataInputRef NVARCHAR(255),
  loopDataOutputRef NVARCHAR(255),
  description NVARCHAR(255),
  sequential BIT,
  dataInputItemRef NVARCHAR(255),
  dataOutputItemRef NVARCHAR(255),
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy NUMERIC(19, 0),
  executedBySubstitute NUMERIC(19, 0),
  activityInstanceId NUMERIC(19, 0),
  aborting BIT NOT NULL,
  triggeredByEvent BIT,
  interrupting BIT,
  PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_afi_kind_lg2_executedBy ON arch_flownode_instance(logicalGroup2, tenantId, kind, executedBy)
GO
CREATE INDEX idx_afi_kind_lg3 ON arch_flownode_instance(tenantId, kind, logicalGroup3)
GO
CREATE INDEX idx_afi_kind_lg4 ON arch_flownode_instance(tenantId, logicalGroup4)
GO
CREATE INDEX idx_afi_sourceId_tenantid_kind ON arch_flownode_instance (sourceObjectId, tenantid, kind)
GO
CREATE INDEX idx1_arch_flownode_instance ON arch_flownode_instance (tenantId, rootContainerId, parentContainerId)
GO

CREATE TABLE arch_connector_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  containerId NUMERIC(19, 0) NOT NULL,
  containerType NVARCHAR(10) NOT NULL,
  connectorId NVARCHAR(255) NOT NULL,
  version NVARCHAR(50) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  activationEvent NVARCHAR(30),
  state NVARCHAR(50),
  sourceObjectId NUMERIC(19, 0),
  archiveDate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (tenantId, containerId, containerType)
GO

CREATE TABLE arch_ref_biz_data_inst (
    tenantid NUMERIC(19, 0) NOT NULL,
    id NUMERIC(19, 0) NOT NULL,
    kind NVARCHAR(15) NOT NULL,
    name NVARCHAR(255) NOT NULL,
    orig_proc_inst_id NUMERIC(19, 0),
    orig_fn_inst_id NUMERIC(19, 0),
    data_id NUMERIC(19, 0),
    data_classname NVARCHAR(255) NOT NULL
)
GO
CREATE INDEX idx_arch_biz_data_inst1 ON arch_ref_biz_data_inst (tenantid, orig_proc_inst_id)
GO
CREATE INDEX idx_arch_biz_data_inst2 ON arch_ref_biz_data_inst (tenantid, orig_fn_inst_id)
GO
ALTER TABLE arch_ref_biz_data_inst ADD CONSTRAINT pk_arch_ref_biz_data_inst PRIMARY KEY (tenantid, id)
GO

CREATE TABLE arch_multi_biz_data (
    tenantid NUMERIC(19, 0) NOT NULL,
    id NUMERIC(19, 0) NOT NULL,
    idx NUMERIC(19, 0) NOT NULL,
    data_id NUMERIC(19, 0) NOT NULL
)
GO
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT pk_arch_rbdi_mbd PRIMARY KEY (tenantid, id, data_id)
GO
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT fk_arch_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES arch_ref_biz_data_inst(tenantid, id) ON DELETE CASCADE
GO

CREATE TABLE arch_data_instance (
    tenantId NUMERIC(19, 0) NOT NULL,
	id NUMERIC(19, 0) NOT NULL,
	name NVARCHAR(50),
	description NVARCHAR(50),
	transientData BIT,
	className NVARCHAR(100),
	containerId NUMERIC(19, 0),
	containerType NVARCHAR(60),
	namespace NVARCHAR(100),
	element NVARCHAR(60),
	intValue INT,
	longValue NUMERIC(19, 0),
	shortTextValue NVARCHAR(255),
	booleanValue BIT,
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue VARBINARY(MAX),
	clobValue NVARCHAR(MAX),
	discriminant NVARCHAR(50) NOT NULL,
	archiveDate NUMERIC(19, 0) NOT NULL,
	sourceObjectId NUMERIC(19, 0) NOT NULL,
	PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx1_arch_data_instance ON arch_data_instance (tenantId, containerId, containerType, archiveDate, name, sourceObjectId)
GO
CREATE INDEX idx2_arch_data_instance ON arch_data_instance (sourceObjectId, containerId, archiveDate, id, tenantId)
GO