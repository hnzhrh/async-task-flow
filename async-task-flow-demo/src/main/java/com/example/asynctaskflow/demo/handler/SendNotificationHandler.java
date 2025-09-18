package com.example.asynctaskflow.demo.handler;

import com.example.asynctaskflow.core.handler.FlowNodeHandler;
import com.example.asynctaskflow.core.handler.NodeExecutionContext;
import com.example.asynctaskflow.core.handler.NodeExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class SendNotificationHandler implements FlowNodeHandler {

    private static final Logger log = LoggerFactory.getLogger(SendNotificationHandler.class);

    @Override
    public String getType() {
        return "send-notification";
    }

    @Override
    public NodeExecutionResult execute(NodeExecutionContext context) {
        String message = String.valueOf(context.getFlowPayload().getOrDefault("message", "No message available"));
        String externalReference = context.getFlowInstance().getExternalReference();
        log.info("[demo] Sending notification for flowInstance={} externalRef={} message={}",
                context.getFlowInstance().getId(), externalReference, message);
        return NodeExecutionResult.complete(Map.of(
                "deliveredAt", Instant.now().toString(),
                "deliveredMessage", message
        ), null);
    }
}
