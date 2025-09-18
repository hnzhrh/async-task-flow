package com.example.asynctaskflow.core.handler;

import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FlowNodeHandlerRegistry {

    private final Map<String, FlowNodeHandler> handlers = new ConcurrentHashMap<>();

    public FlowNodeHandlerRegistry(Collection<FlowNodeHandler> handlers) {
        if (handlers != null) {
            handlers.forEach(this::register);
        }
    }

    public void register(FlowNodeHandler handler) {
        Assert.notNull(handler, "handler must not be null");
        String type = handler.getType();
        Assert.hasText(type, "handler type must not be blank");
        handlers.put(type, handler);
    }

    public FlowNodeHandler getHandler(String type) {
        return handlers.get(type);
    }

    public Map<String, FlowNodeHandler> getHandlers() {
        return Collections.unmodifiableMap(handlers);
    }
}
