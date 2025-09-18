package com.example.asynctaskflow.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "flow_node_definitions")
public class FlowNodeDefinition extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flow_definition_id", nullable = false)
    private FlowDefinition flowDefinition;

    @Column(name = "sequence_index", nullable = false)
    private int sequence;

    @Column(nullable = false, length = 128)
    private String type;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Lob
    @Column(name = "configuration_json")
    private String configurationJson;

    @Column(name = "allow_manual_trigger", nullable = false)
    private boolean allowManualTrigger = true;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    public Long getId() {
        return id;
    }

    public FlowDefinition getFlowDefinition() {
        return flowDefinition;
    }

    public void setFlowDefinition(FlowDefinition flowDefinition) {
        this.flowDefinition = flowDefinition;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getConfigurationJson() {
        return configurationJson;
    }

    public void setConfigurationJson(String configurationJson) {
        this.configurationJson = configurationJson;
    }

    public boolean isAllowManualTrigger() {
        return allowManualTrigger;
    }

    public void setAllowManualTrigger(boolean allowManualTrigger) {
        this.allowManualTrigger = allowManualTrigger;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
}
