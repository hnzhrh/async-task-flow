package com.example.asynctaskflow.demo.web;

public class DemoNodeView {

    private final Long id;
    private final String displayName;
    private final String type;
    private final String status;
    private final int attempts;
    private final String lastError;

    public DemoNodeView(Long id, String displayName, String type, String status, int attempts, String lastError) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.status = status;
        this.attempts = attempts;
        this.lastError = lastError;
    }

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getLastError() {
        return lastError;
    }
}
