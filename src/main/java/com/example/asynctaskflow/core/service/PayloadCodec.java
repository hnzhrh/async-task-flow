package com.example.asynctaskflow.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;

public class PayloadCodec {

    private static final Logger log = LoggerFactory.getLogger(PayloadCodec.class);

    private final ObjectMapper objectMapper;

    public PayloadCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> toMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize JSON payload, returning empty map", e);
            return Collections.emptyMap();
        }
    }

    public String toJson(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException("Failed to serialize payload to JSON", e);
        }
    }
}
