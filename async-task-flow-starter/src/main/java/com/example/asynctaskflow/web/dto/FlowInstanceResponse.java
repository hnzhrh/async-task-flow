package com.example.asynctaskflow.web.dto;

import com.example.asynctaskflow.core.model.FlowStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class FlowInstanceResponse {

    private Long id;
    private Long flowDefinitionId;
    private FlowStatus status;
    private String externalReference;
    private Instant startedAt;
    private Instant completedAt;
    private String lastError;
    private Map<String, Object> payload;
    private List<NodeInstanceResponse> nodes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFlowDefinitionId() {
        return flowDefinitionId;
    }

    public void setFlowDefinitionId(Long flowDefinitionId) {
        this.flowDefinitionId = flowDefinitionId;
    }

    public FlowStatus getStatus() {
        return status;
    }

    public void setStatus(FlowStatus status) {
        this.status = status;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public List<NodeInstanceResponse> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeInstanceResponse> nodes) {
        this.nodes = nodes;
    }
}
