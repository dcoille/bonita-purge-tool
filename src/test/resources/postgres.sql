CREATE TABLE tenant (
    id INT8 NOT NULL,
    description VARCHAR(255),
    iconname VARCHAR(50),
    iconpath VARCHAR(255),
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);
CREATE TABLE process_definition (
    tenantid INT8 NOT NULL,
    id INT8 NOT NULL,
    processId INT8 NOT NULL,
    name VARCHAR(150) NOT NULL,
    version VARCHAR(50) NOT NULL,
    lastUpdateDate INT8,
    categoryId INT8,
    iconPath VARCHAR(255),
    PRIMARY KEY (tenantid, id),
    UNIQUE (tenantid, name)
);
CREATE TABLE arch_contract_data (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  kind VARCHAR(20) NOT NULL,
  scopeId INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  val TEXT,
  archiveDate INT8 NOT NULL,
  sourceObjectId INT8 NOT NULL
);
ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id, scopeId);
ALTER TABLE arch_contract_data ADD CONSTRAINT uc_acd_scope_name UNIQUE (kind, scopeId, name, tenantid);
CREATE INDEX idx_acd_scope_name ON arch_contract_data (kind, scopeId, name, tenantid);

CREATE TABLE arch_process_comment(
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  processInstanceId INT8 NOT NULL,
  archiveDate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE arch_document_mapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  sourceObjectId INT8,
  processinstanceid INT8 NOT NULL,
  documentid INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  archiveDate INT8 NOT NULL,
  PRIMARY KEY (tenantid, ID)
);
CREATE INDEX idx_a_doc_mp_pr_id ON arch_document_mapping (processinstanceid, tenantid);

CREATE TABLE arch_process_instance (
   tenantid INT8 NOT NULL,
   id INT8 NOT NULL,
   name VARCHAR(75) NOT NULL,
   processDefinitionId INT8 NOT NULL,
   description VARCHAR(255),
   endDate INT8 NOT NULL,
   stateId INT NOT NULL,
   rootProcessInstanceId INT8,
   callerId INT8,
   sourceObjectId INT8 NOT NULL,
   stringIndex1 VARCHAR(255),
   stringIndex2 VARCHAR(255),
   stringIndex3 VARCHAR(255),
   stringIndex4 VARCHAR(255),
   stringIndex5 VARCHAR(255),
   PRIMARY KEY (tenantid, id)
);

CREATE TABLE arch_flownode_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  flownodeDefinitionId INT8 NOT NULL,
  kind VARCHAR(25) NOT NULL,
  sourceObjectId INT8,
  archiveDate INT8 NOT NULL,
  rootContainerId INT8 NOT NULL,
  stable BOOLEAN ,
  reachedStateDate INT8,
  lastUpdateDate INT8,
  expectedEndDate INT8,
  claimedDate INT8,
  priority SMALLINT,
  gatewayType VARCHAR(50),
  hitBys VARCHAR(255),
  logicalGroup3 INT8,
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
  executedBy INT8,
  executedBySubstitute INT8,
  activityInstanceId INT8,
  triggeredByEvent BOOLEAN,
  interrupting BOOLEAN,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_afi_kind_lg3 ON arch_flownode_instance(tenantId, kind, logicalGroup3);
CREATE INDEX idx_afi_sourceId_tenantid_kind ON arch_flownode_instance (sourceObjectId, tenantid, kind);

CREATE TABLE arch_connector_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  containerId INT8 NOT NULL,
  containerType VARCHAR(10) NOT NULL,
  version VARCHAR(50) NOT NULL,
  name VARCHAR(255) NOT NULL,
  activationEvent VARCHAR(30),
  state VARCHAR(50),
  sourceObjectId INT8,
  archiveDate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (tenantId, containerId, containerType);

CREATE TABLE arch_ref_biz_data_inst (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	orig_proc_inst_id BIGINT,
  	orig_fn_inst_id BIGINT,
  	data_id BIGINT
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
    tenantId INT8 NOT NULL,
	id INT8 NOT NULL,
	name VARCHAR(50),
	description VARCHAR(50),
	transientData BOOLEAN,
	className VARCHAR(100),
	containerId INT8,
	containerType VARCHAR(60),
	namespace VARCHAR(100),
	element VARCHAR(60),
	intValue INT,
	longValue INT8,
	shortTextValue VARCHAR(255),
	booleanValue BOOLEAN,
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue BYTEA,
	clobValue TEXT,
	archiveDate INT8 NOT NULL,
	PRIMARY KEY (tenantid, id)
);