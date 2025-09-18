package com.example.asynctaskflow.demo.handler;

import com.example.asynctaskflow.core.handler.FlowNodeHandler;
import com.example.asynctaskflow.core.handler.NodeExecutionContext;
import com.example.asynctaskflow.core.handler.NodeExecutionResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PrepareMessageHandler implements FlowNodeHandler {

    @Override
    public String getType() {
        return "prepare-message";
    }

    @Override
    public NodeExecutionResult execute(NodeExecutionContext context) {
        String name = String.valueOf(context.getFlowPayload().getOrDefault("name", "friend"));
        String greeting = String.valueOf(context.getNodeConfiguration().getOrDefault("greeting", "Hello"));
        String message = greeting + ", " + name + "!";
        Map<String, Object> update = Map.of("message", message);
        return NodeExecutionResult.complete(update, update);
    }
}
