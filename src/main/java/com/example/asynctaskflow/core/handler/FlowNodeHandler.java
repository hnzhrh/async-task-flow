package com.example.asynctaskflow.core.handler;

public interface FlowNodeHandler {

    String getType();

    NodeExecutionResult execute(NodeExecutionContext context) throws Exception;
}
