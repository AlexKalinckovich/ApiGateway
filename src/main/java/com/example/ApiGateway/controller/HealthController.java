package com.example.ApiGateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/actuator/health/readiness")
    public ResponseEntity<ObjectNode> readiness() {
        final ObjectNode response = objectMapper.createObjectNode();
        response.put("status", "UP");

        final ObjectNode components = objectMapper.createObjectNode();
        final ObjectNode readinessState = objectMapper.createObjectNode();
        readinessState.put("status", "UP");
        components.set("readinessState", readinessState);

        response.set("components", components);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/actuator/health/liveness")
    public ResponseEntity<ObjectNode> liveness() {
        final ObjectNode response = objectMapper.createObjectNode();
        response.put("status", "UP");
        return ResponseEntity.ok(response);
    }
}
