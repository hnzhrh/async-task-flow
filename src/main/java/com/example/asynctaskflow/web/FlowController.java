package com.example.asynctaskflow.web;

import com.example.asynctaskflow.core.model.FlowInstance;
import com.example.asynctaskflow.core.model.FlowNodeInstance;
import com.example.asynctaskflow.core.service.FlowEngine;
import com.example.asynctaskflow.core.service.PayloadCodec;
import com.example.asynctaskflow.web.dto.FlowInstanceResponse;
import com.example.asynctaskflow.web.dto.FlowTriggerRequest;
import com.example.asynctaskflow.web.dto.NodeInstanceResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/async-flow")
@Validated
public class FlowController {

    private final FlowEngine flowEngine;
    private final PayloadCodec payloadCodec;

    public FlowController(FlowEngine flowEngine, PayloadCodec payloadCodec) {
        this.flowEngine = flowEngine;
        this.payloadCodec = payloadCodec;
    }

    @PostMapping("/instances")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public FlowInstanceResponse startFlow(@Valid @RequestBody FlowTriggerRequest request) {
        FlowInstance instance = flowEngine.startFlow(request.getFlowCode(), request.getVersion(),
                request.getPayload(), request.getExternalReference());
        List<FlowNodeInstance> nodeInstances = flowEngine.getNodeInstances(instance.getId());
        return toResponse(instance, nodeInstances);
    }

    @PostMapping("/nodes/{nodeId}/trigger")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void triggerNode(@PathVariable Long nodeId) {
        flowEngine.triggerNode(nodeId);
    }

    @GetMapping("/instances/{instanceId}")
    @Transactional(readOnly = true)
    public FlowInstanceResponse getInstance(@PathVariable Long instanceId) {
        FlowInstance instance = flowEngine.getFlowInstance(instanceId);
        List<FlowNodeInstance> nodeInstances = flowEngine.getNodeInstances(instance.getId());
        return toResponse(instance, nodeInstances);
    }

    private FlowInstanceResponse toResponse(FlowInstance instance, List<FlowNodeInstance> nodes) {
        FlowInstanceResponse response = new FlowInstanceResponse();
        response.setId(instance.getId());
        response.setFlowDefinitionId(instance.getFlowDefinition().getId());
        response.setStatus(instance.getStatus());
        response.setExternalReference(instance.getExternalReference());
        response.setStartedAt(instance.getStartedAt());
        response.setCompletedAt(instance.getCompletedAt());
        response.setLastError(instance.getLastError());
        response.setPayload(payloadCodec.toMap(instance.getPayloadJson()));
        response.setNodes(nodes.stream().map(this::toNodeResponse).collect(Collectors.toList()));
        return response;
    }

    private NodeInstanceResponse toNodeResponse(FlowNodeInstance nodeInstance) {
        NodeInstanceResponse response = new NodeInstanceResponse();
        response.setId(nodeInstance.getId());
        response.setNodeDefinitionId(nodeInstance.getNodeDefinition().getId());
        response.setNodeType(nodeInstance.getNodeDefinition().getType());
        response.setSequence(nodeInstance.getNodeDefinition().getSequence());
        response.setStatus(nodeInstance.getStatus());
        response.setAttemptCount(nodeInstance.getAttemptCount());
        response.setLastError(nodeInstance.getLastError());
        response.setStartedAt(nodeInstance.getStartedAt());
        response.setCompletedAt(nodeInstance.getCompletedAt());
        return response;
    }
}
