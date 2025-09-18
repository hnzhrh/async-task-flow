package com.example.asynctaskflow.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public class FlowTriggerRequest {

    @NotBlank
    private String flowCode;

    private Integer version;

    private String externalReference;

    private Map<String, Object> payload;

    public String getFlowCode() {
        return flowCode;
    }

    public void setFlowCode(String flowCode) {
        this.flowCode = flowCode;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
