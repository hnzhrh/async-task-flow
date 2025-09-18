package com.example.asynctaskflow.core.handler;

import com.example.asynctaskflow.core.model.FlowDefinition;
import com.example.asynctaskflow.core.model.FlowInstance;
import com.example.asynctaskflow.core.model.FlowNodeDefinition;
import com.example.asynctaskflow.core.model.FlowNodeInstance;

import java.util.Map;

public class NodeExecutionContext {

    private final FlowDefinition flowDefinition;
    private final FlowInstance flowInstance;
    private final FlowNodeDefinition nodeDefinition;
    private final FlowNodeInstance nodeInstance;
    private final Map<String, Object> flowPayload;
    private final Map<String, Object> nodePayload;
    private final Map<String, Object> nodeConfiguration;

    public NodeExecutionContext(FlowDefinition flowDefinition,
                                FlowInstance flowInstance,
                                FlowNodeDefinition nodeDefinition,
                                FlowNodeInstance nodeInstance,
                                Map<String, Object> flowPayload,
                                Map<String, Object> nodePayload,
                                Map<String, Object> nodeConfiguration) {
        this.flowDefinition = flowDefinition;
        this.flowInstance = flowInstance;
        this.nodeDefinition = nodeDefinition;
        this.nodeInstance = nodeInstance;
        this.flowPayload = flowPayload;
        this.nodePayload = nodePayload;
        this.nodeConfiguration = nodeConfiguration;
    }

    public FlowDefinition getFlowDefinition() {
        return flowDefinition;
    }

    public FlowInstance getFlowInstance() {
        return flowInstance;
    }

    public FlowNodeDefinition getNodeDefinition() {
        return nodeDefinition;
    }

    public FlowNodeInstance getNodeInstance() {
        return nodeInstance;
    }

    public Map<String, Object> getFlowPayload() {
        return flowPayload;
    }

    public Map<String, Object> getNodePayload() {
        return nodePayload;
    }

    public Map<String, Object> getNodeConfiguration() {
        return nodeConfiguration;
    }
}
