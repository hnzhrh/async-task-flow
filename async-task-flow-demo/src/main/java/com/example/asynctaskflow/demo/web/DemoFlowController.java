package com.example.asynctaskflow.demo.web;

import com.example.asynctaskflow.core.model.FlowInstance;
import com.example.asynctaskflow.core.model.FlowNodeInstance;
import com.example.asynctaskflow.core.service.FlowEngine;
import com.example.asynctaskflow.core.service.PayloadCodec;
import com.example.asynctaskflow.demo.config.DemoFlowInitializer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/demo/flows")
@Validated
public class DemoFlowController {

    private final FlowEngine flowEngine;
    private final PayloadCodec payloadCodec;

    public DemoFlowController(FlowEngine flowEngine, PayloadCodec payloadCodec) {
        this.flowEngine = flowEngine;
        this.payloadCodec = payloadCodec;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public DemoFlowResponse startDemoFlow(@Valid @RequestBody DemoFlowRequest request) {
        FlowInstance instance = flowEngine.startFlow(DemoFlowInitializer.DEMO_FLOW_CODE, 1,
                Map.of("name", request.getName(), "contact", request.getContact()), request.getExternalReference());
        return buildResponse(instance);
    }

    @GetMapping("/{instanceId}")
    public DemoFlowResponse getFlow(@PathVariable Long instanceId) {
        FlowInstance instance = flowEngine.getFlowInstance(instanceId);
        return buildResponse(instance);
    }

    private DemoFlowResponse buildResponse(FlowInstance instance) {
        List<DemoNodeView> nodes = flowEngine.getNodeInstances(instance.getId()).stream()
                .map(this::toNodeView)
                .collect(Collectors.toList());
        return new DemoFlowResponse(instance.getId(), instance.getStatus().name(),
                payloadCodec.toMap(instance.getPayloadJson()), nodes);
    }

    private DemoNodeView toNodeView(FlowNodeInstance nodeInstance) {
        return new DemoNodeView(nodeInstance.getId(),
                nodeInstance.getNodeDefinition().getDisplayName(),
                nodeInstance.getNodeDefinition().getType(),
                nodeInstance.getStatus().name(),
                nodeInstance.getAttemptCount(),
                nodeInstance.getLastError());
    }
}
