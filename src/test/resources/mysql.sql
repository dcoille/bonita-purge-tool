CREATE TABLE arch_contract_data (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  kind VARCHAR(20) NOT NULL,
  scopeId BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  val LONGTEXT,
  archiveDate BIGINT NOT NULL,
  sourceObjectId BIGINT NOT NULL
) ENGINE = INNODB;
ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id, scopeId);
ALTER TABLE arch_contract_data ADD CONSTRAINT uc_acd_scope_name UNIQUE (kind, scopeId, name, tenantid);
CREATE INDEX idx_acd_scope_name ON arch_contract_data (kind, scopeId, name, tenantid);

CREATE TABLE arch_process_comment(
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  userId BIGINT,
  processInstanceId BIGINT NOT NULL,
  postDate BIGINT NOT NULL,
  content VARCHAR(512) NOT NULL,
  archiveDate BIGINT NOT NULL,
  sourceObjectId BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx1_arch_process_comment on arch_process_comment (sourceobjectid, tenantid);
CREATE INDEX idx2_arch_process_comment on arch_process_comment (processInstanceId, archivedate, tenantid);

CREATE TABLE arch_document_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  sourceObjectId BIGINT,
  processinstanceid BIGINT NOT NULL,
  documentid BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  version VARCHAR(50) NOT NULL,
  index_ INT NOT NULL,
  archiveDate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, ID)
) ENGINE = INNODB;
CREATE INDEX idx_a_doc_mp_pr_id ON arch_document_mapping (processinstanceid, tenantid);

CREATE TABLE arch_process_instance (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(75) NOT NULL,
  processDefinitionId BIGINT NOT NULL,
  description VARCHAR(255),
  startDate BIGINT NOT NULL,
  startedBy BIGINT NOT NULL,
  startedBySubstitute BIGINT NOT NULL,
  endDate BIGINT NOT NULL,
  archiveDate BIGINT NOT NULL,
  stateId INT NOT NULL,
  lastUpdate BIGINT NOT NULL,
  rootProcessInstanceId BIGINT,
  callerId BIGINT,
  sourceObjectId BIGINT NOT NULL,
  stringIndex1 VARCHAR(255),
  stringIndex2 VARCHAR(255),
  stringIndex3 VARCHAR(255),
  stringIndex4 VARCHAR(255),
  stringIndex5 VARCHAR(255),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx1_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, rootProcessInstanceId, callerId);
CREATE INDEX idx2_arch_process_instance ON arch_process_instance (tenantId, processDefinitionId, archiveDate);
CREATE INDEX idx3_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, callerId, stateId);

CREATE TABLE arch_flownode_instance (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  flownodeDefinitionId BIGINT NOT NULL,
  kind VARCHAR(25) NOT NULL,
  sourceObjectId BIGINT,
  archiveDate BIGINT NOT NULL,
  rootContainerId BIGINT NOT NULL,
  parentContainerId BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  displayName VARCHAR(255),
  displayDescription VARCHAR(255),
  stateId INT NOT NULL,
  stateName VARCHAR(50),
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  actorId BIGINT NULL,
  assigneeId BIGINT DEFAULT 0 NOT NULL,
  reachedStateDate BIGINT,
  lastUpdateDate BIGINT,
  expectedEndDate BIGINT,
  claimedDate BIGINT,
  priority TINYINT,
  gatewayType VARCHAR(50),
  hitBys VARCHAR(255),
  logicalGroup1 BIGINT NOT NULL,
  logicalGroup2 BIGINT NOT NULL,
  logicalGroup3 BIGINT,
  logicalGroup4 BIGINT NOT NULL,
  loop_counter INT,
  loop_max INT,
  loopCardinality INT,
  loopDataInputRef VARCHAR(255),
  loopDataOutputRef VARCHAR(255),
  description VARCHAR(255),
  sequential BOOLEAN,
  dataInputItemRef VARCHAR(255),
  dataOutputItemRef VARCHAR(255),
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy BIGINT,
  executedBySubstitute BIGINT,
  activityInstanceId BIGINT,
  aborting BOOLEAN NOT NULL,
  triggeredByEvent BOOLEAN,
  interrupting BOOLEAN,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_afi_kind_lg2_executedBy ON arch_flownode_instance(logicalGroup2, tenantId, kind, executedBy);
CREATE INDEX idx_afi_kind_lg3 ON arch_flownode_instance(tenantId, kind, logicalGroup3);
CREATE INDEX idx_afi_kind_lg4 ON arch_flownode_instance(tenantId, logicalGroup4);
CREATE INDEX idx_afi_sourceId_tenantid_kind ON arch_flownode_instance (sourceObjectId, tenantid, kind);
CREATE INDEX idx1_arch_flownode_instance ON arch_flownode_instance (tenantId, rootContainerId, parentContainerId);

CREATE TABLE arch_connector_instance (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  containerId BIGINT NOT NULL,
  containerType VARCHAR(10) NOT NULL,
  connectorId VARCHAR(255) NOT NULL,
  version VARCHAR(50) NOT NULL,
  name VARCHAR(255) NOT NULL,
  activationEvent VARCHAR(30),
  state VARCHAR(50),
  sourceObjectId BIGINT,
  archiveDate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (tenantId, containerId, containerType);

CREATE TABLE arch_ref_biz_data_inst (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	name VARCHAR(255) NOT NULL,
  	orig_proc_inst_id BIGINT,
  	orig_fn_inst_id BIGINT,
  	data_id BIGINT,
  	data_classname VARCHAR(255) NOT NULL
);
CREATE INDEX idx_arch_biz_data_inst1 ON arch_ref_biz_data_inst (tenantid, orig_proc_inst_id);
CREATE INDEX idx_arch_biz_data_inst2 ON arch_ref_biz_data_inst (tenantid, orig_fn_inst_id);
ALTER TABLE arch_ref_biz_data_inst ADD CONSTRAINT pk_arch_ref_biz_data_inst PRIMARY KEY (tenantid, id);

CREATE TABLE arch_multi_biz_data (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	idx BIGINT NOT NULL,
  	data_id BIGINT NOT NULL
);
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT pk_arch_rbdi_mbd PRIMARY KEY (tenantid, id, data_id);
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT fk_arch_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES arch_ref_biz_data_inst(tenantid, id) ON DELETE CASCADE;

CREATE TABLE arch_data_instance (
    tenantId BIGINT NOT NULL,
	id BIGINT NOT NULL,
	name VARCHAR(50),
	description VARCHAR(50),
	transientData BOOLEAN,
	className VARCHAR(100),
	containerId BIGINT,
	containerType VARCHAR(60),
	namespace VARCHAR(100),
	element VARCHAR(60),
	intValue INT,
	longValue BIGINT,
	shortTextValue VARCHAR(255),
	booleanValue BOOLEAN,
	doubleValue NUMERIC(19,5),
	floatValue FLOAT,
	blobValue MEDIUMBLOB,
	clobValue MEDIUMTEXT,
	discriminant VARCHAR(50) NOT NULL,
	archiveDate BIGINT NOT NULL,
	sourceObjectId BIGINT NOT NULL,
	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE INDEX idx1_arch_data_instance ON arch_data_instance (tenantId, containerId, containerType, archiveDate, name, sourceObjectId);
CREATE INDEX idx2_arch_data_instance ON arch_data_instance (sourceObjectId, containerId, archiveDate, id, tenantId);