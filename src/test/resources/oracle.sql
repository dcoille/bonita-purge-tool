CREATE TABLE arch_contract_data (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(20 CHAR) NOT NULL,
  scopeId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  val CLOB,
  archiveDate NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0) NOT NULL
);
ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id, scopeId);
ALTER TABLE arch_contract_data ADD CONSTRAINT uc_acd_scope_name UNIQUE (kind, scopeId, name, tenantid);

CREATE TABLE arch_process_comment(
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0),
  processInstanceId NUMBER(19, 0) NOT NULL,
  postDate NUMBER(19, 0) NOT NULL,
  content VARCHAR2(512 CHAR) NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_process_comment on arch_process_comment (sourceobjectid, tenantid);
CREATE INDEX idx2_arch_process_comment on arch_process_comment (processInstanceId, archivedate, tenantid);

CREATE TABLE arch_document_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0),
  processinstanceid NUMBER(19, 0) NOT NULL,
  documentid NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  version VARCHAR2(50 CHAR) NOT NULL,
  index_ INT NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_a_doc_mp_pr_id ON arch_document_mapping (processinstanceid, tenantid);

CREATE TABLE arch_process_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(75 CHAR) NOT NULL,
  processDefinitionId NUMBER(19, 0) NOT NULL,
  description VARCHAR2(255 CHAR),
  startDate NUMBER(19, 0) NOT NULL,
  startedBy NUMBER(19, 0) NOT NULL,
  startedBySubstitute NUMBER(19, 0) NOT NULL,
  endDate NUMBER(19, 0) NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  stateId INT NOT NULL,
  lastUpdate NUMBER(19, 0) NOT NULL,
  rootProcessInstanceId NUMBER(19, 0),
  callerId NUMBER(19, 0),
  sourceObjectId NUMBER(19, 0) NOT NULL,
  stringIndex1 VARCHAR2(255 CHAR),
  stringIndex2 VARCHAR2(255 CHAR),
  stringIndex3 VARCHAR2(255 CHAR),
  stringIndex4 VARCHAR2(255 CHAR),
  stringIndex5 VARCHAR2(255 CHAR),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, rootProcessInstanceId, callerId);
CREATE INDEX idx2_arch_process_instance ON arch_process_instance (tenantId, processDefinitionId, archiveDate);
CREATE INDEX idx3_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, callerId, stateId);

CREATE TABLE arch_flownode_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  flownodeDefinitionId NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(25 CHAR) NOT NULL,
  sourceObjectId NUMBER(19, 0),
  archiveDate NUMBER(19, 0) NOT NULL,
  rootContainerId NUMBER(19, 0) NOT NULL,
  parentContainerId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  displayName VARCHAR2(255 CHAR),
  displayDescription VARCHAR2(255 CHAR),
  stateId INT NOT NULL,
  stateName VARCHAR2(50 CHAR),
  terminal NUMBER(1) NOT NULL,
  stable NUMBER(1) ,
  actorId NUMBER(19, 0) NULL,
  assigneeId NUMBER(19, 0) DEFAULT 0 NOT NULL,
  reachedStateDate NUMBER(19, 0),
  lastUpdateDate NUMBER(19, 0),
  expectedEndDate NUMBER(19, 0),
  claimedDate NUMBER(19, 0),
  priority SMALLINT,
  gatewayType VARCHAR2(50 CHAR),
  hitBys VARCHAR2(255 CHAR),
  logicalGroup1 NUMBER(19, 0) NOT NULL,
  logicalGroup2 NUMBER(19, 0) NOT NULL,
  logicalGroup3 NUMBER(19, 0),
  logicalGroup4 NUMBER(19, 0) NOT NULL,
  loop_counter INT,
  loop_max INT,
  loopCardinality INT,
  loopDataInputRef VARCHAR2(255 CHAR),
  loopDataOutputRef VARCHAR2(255 CHAR),
  description VARCHAR2(255 CHAR),
  sequential NUMBER(1),
  dataInputItemRef VARCHAR2(255 CHAR),
  dataOutputItemRef VARCHAR2(255 CHAR),
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy NUMBER(19, 0),
  executedBySubstitute NUMBER(19, 0),
  activityInstanceId NUMBER(19, 0),
  aborting NUMBER(1) NOT NULL,
  triggeredByEvent NUMBER(1),
  interrupting NUMBER(1),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_afi_kind_lg2_executedBy ON arch_flownode_instance(logicalGroup2, tenantId, kind, executedBy);
CREATE INDEX idx_afi_kind_lg3 ON arch_flownode_instance(tenantId, kind, logicalGroup3);
CREATE INDEX idx_afi_kind_lg4 ON arch_flownode_instance(tenantId, logicalGroup4);
CREATE INDEX idx_afi_sourceId_tenantid_kind ON arch_flownode_instance (sourceObjectId, tenantid, kind);
CREATE INDEX idx1_arch_flownode_instance ON arch_flownode_instance (tenantId, rootContainerId, parentContainerId);

CREATE TABLE arch_connector_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  containerId NUMBER(19, 0) NOT NULL,
  containerType VARCHAR2(10 CHAR) NOT NULL,
  connectorId VARCHAR2(255 CHAR) NOT NULL,
  version VARCHAR2(50 CHAR) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  activationEvent VARCHAR2(30 CHAR),
  state VARCHAR2(50 CHAR),
  sourceObjectId NUMBER(19, 0),
  archiveDate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (tenantId, containerId, containerType);

CREATE TABLE arch_ref_biz_data_inst (
    tenantid NUMBER(19, 0) NOT NULL,
    id NUMBER(19, 0) NOT NULL,
    kind VARCHAR2(15 CHAR) NOT NULL,
    name VARCHAR2(255 CHAR) NOT NULL,
    orig_proc_inst_id NUMBER(19, 0),
    orig_fn_inst_id NUMBER(19, 0),
    data_id NUMBER(19, 0),
    data_classname VARCHAR2(255 CHAR) NOT NULL
);
CREATE INDEX idx_arch_biz_data_inst1 ON arch_ref_biz_data_inst (tenantid, orig_proc_inst_id);
CREATE INDEX idx_arch_biz_data_inst2 ON arch_ref_biz_data_inst (tenantid, orig_fn_inst_id);
ALTER TABLE arch_ref_biz_data_inst ADD CONSTRAINT pk_arch_ref_biz_data_inst PRIMARY KEY (tenantid, id);

CREATE TABLE arch_multi_biz_data (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	idx NUMBER(19, 0) NOT NULL,
  	data_id NUMBER(19, 0) NOT NULL
);
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT pk_arch_rbdi_mbd PRIMARY KEY (tenantid, id, data_id);
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT fk_arch_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES arch_ref_biz_data_inst(tenantid, id) ON DELETE CASCADE;

CREATE TABLE arch_data_instance (
    tenantId NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	name VARCHAR2(50 CHAR),
	description VARCHAR2(50 CHAR),
	transientData NUMBER(1),
	className VARCHAR2(100 CHAR),
	containerId NUMBER(19, 0),
	containerType VARCHAR2(60 CHAR),
	namespace VARCHAR2(100 CHAR),
	element VARCHAR2(60 CHAR),
	intValue INT,
	longValue NUMBER(19, 0),
	shortTextValue VARCHAR2(255 CHAR),
	booleanValue NUMBER(1),
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue BLOB,
	clobValue CLOB,
	discriminant VARCHAR2(50 CHAR) NOT NULL,
	archiveDate NUMBER(19, 0) NOT NULL,
	sourceObjectId NUMBER(19, 0) NOT NULL,
	PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_data_instance ON arch_data_instance (tenantId, containerId, containerType, archiveDate, name, sourceObjectId);
CREATE INDEX idx2_arch_data_instance ON arch_data_instance (sourceObjectId, containerId, archiveDate, id, tenantId);