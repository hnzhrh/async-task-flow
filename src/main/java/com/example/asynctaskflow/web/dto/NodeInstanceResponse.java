package com.example.asynctaskflow.web.dto;

import com.example.asynctaskflow.core.model.NodeStatus;

import java.time.Instant;

public class NodeInstanceResponse {

    private Long id;
    private Long nodeDefinitionId;
    private String nodeType;
    private int sequence;
    private NodeStatus status;
    private int attemptCount;
    private String lastError;
    private Instant startedAt;
    private Instant completedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNodeDefinitionId() {
        return nodeDefinitionId;
    }

    public void setNodeDefinitionId(Long nodeDefinitionId) {
        this.nodeDefinitionId = nodeDefinitionId;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
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
}
