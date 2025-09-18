package com.example.asynctaskflow.core.handler;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class NodeExecutionResult {

    public enum Status {
        COMPLETE,
        RETRY,
        FAILED
    }

    private final Status status;
    private final Duration retryDelay;
    private final Map<String, Object> flowPayloadUpdates;
    private final Map<String, Object> nodePayloadUpdates;
    private final String message;

    private NodeExecutionResult(Status status,
                                Duration retryDelay,
                                Map<String, Object> flowPayloadUpdates,
                                Map<String, Object> nodePayloadUpdates,
                                String message) {
        this.status = status;
        this.retryDelay = retryDelay;
        this.flowPayloadUpdates = flowPayloadUpdates;
        this.nodePayloadUpdates = nodePayloadUpdates;
        this.message = message;
    }

    public static NodeExecutionResult complete(Map<String, Object> flowPayloadUpdates,
                                               Map<String, Object> nodePayloadUpdates) {
        return new NodeExecutionResult(Status.COMPLETE, null, flowPayloadUpdates, nodePayloadUpdates, null);
    }

    public static NodeExecutionResult complete() {
        return complete(null, null);
    }

    public static NodeExecutionResult retry(Duration retryDelay, String message) {
        Objects.requireNonNull(retryDelay, "retryDelay is required");
        return new NodeExecutionResult(Status.RETRY, retryDelay, null, null, message);
    }

    public static NodeExecutionResult failed(String message) {
        return new NodeExecutionResult(Status.FAILED, null, null, null, message);
    }

    public Status getStatus() {
        return status;
    }

    public Duration getRetryDelay() {
        return retryDelay;
    }

    public Map<String, Object> getFlowPayloadUpdates() {
        return flowPayloadUpdates;
    }

    public Map<String, Object> getNodePayloadUpdates() {
        return nodePayloadUpdates;
    }

    public String getMessage() {
        return message;
    }
}
