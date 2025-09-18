package com.example.asynctaskflow.demo.web;

import jakarta.validation.constraints.NotBlank;

public class DemoFlowRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String contact;

    private String externalReference;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }
}
