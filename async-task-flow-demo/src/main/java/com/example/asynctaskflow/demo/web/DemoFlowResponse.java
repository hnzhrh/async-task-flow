package com.example.asynctaskflow.demo.web;

import java.util.List;
import java.util.Map;

public class DemoFlowResponse {

    private final Long id;
    private final String status;
    private final Map<String, Object> payload;
    private final List<DemoNodeView> nodes;

    public DemoFlowResponse(Long id, String status, Map<String, Object> payload, List<DemoNodeView> nodes) {
        this.id = id;
        this.status = status;
        this.payload = payload;
        this.nodes = nodes;
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public List<DemoNodeView> getNodes() {
        return nodes;
    }
}
