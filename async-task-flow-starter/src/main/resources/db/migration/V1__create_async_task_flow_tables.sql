CREATE TABLE flow_definitions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(128) NOT NULL,
    name VARCHAR(255) NOT NULL,
    version INT NOT NULL,
    description VARCHAR(1000),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    entity_version BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_flow_definition_code_version (code, version)
) ENGINE = InnoDB;

CREATE TABLE flow_node_definitions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    flow_definition_id BIGINT NOT NULL,
    sequence_index INT NOT NULL,
    type VARCHAR(128) NOT NULL,
    display_name VARCHAR(255),
    configuration_json LONGTEXT,
    allow_manual_trigger BIT(1) NOT NULL DEFAULT b'1',
    max_attempts INT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_node_definition_flow_definition FOREIGN KEY (flow_definition_id) REFERENCES flow_definitions (id) ON DELETE CASCADE,
    KEY idx_node_definition_flow_sequence (flow_definition_id, sequence_index)
) ENGINE = InnoDB;

CREATE TABLE flow_instances (
    id BIGINT NOT NULL AUTO_INCREMENT,
    flow_definition_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    external_reference VARCHAR(128),
    payload_json LONGTEXT,
    started_at DATETIME(6),
    completed_at DATETIME(6),
    last_error VARCHAR(2000),
    locked_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    entity_version BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_flow_instance_definition FOREIGN KEY (flow_definition_id) REFERENCES flow_definitions (id),
    KEY idx_flow_instance_definition (flow_definition_id),
    KEY idx_flow_instance_status (status)
) ENGINE = InnoDB;

CREATE TABLE flow_node_instances (
    id BIGINT NOT NULL AUTO_INCREMENT,
    flow_instance_id BIGINT NOT NULL,
    node_definition_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempt_count INT NOT NULL,
    last_error VARCHAR(2000),
    started_at DATETIME(6),
    completed_at DATETIME(6),
    next_attempt_at DATETIME(6),
    locked_at DATETIME(6),
    context_json LONGTEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    entity_version BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_node_instance_flow_instance FOREIGN KEY (flow_instance_id) REFERENCES flow_instances (id) ON DELETE CASCADE,
    CONSTRAINT fk_node_instance_node_definition FOREIGN KEY (node_definition_id) REFERENCES flow_node_definitions (id),
    KEY idx_node_instance_flow (flow_instance_id),
    KEY idx_node_instance_status (status, next_attempt_at),
    KEY idx_node_instance_locked (locked_at)
) ENGINE = InnoDB;
