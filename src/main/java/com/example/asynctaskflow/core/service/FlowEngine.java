package com.example.asynctaskflow.core.service;

import com.example.asynctaskflow.core.model.FlowDefinition;
import com.example.asynctaskflow.core.model.FlowInstance;
import com.example.asynctaskflow.core.model.FlowNodeDefinition;
import com.example.asynctaskflow.core.model.FlowNodeInstance;
import com.example.asynctaskflow.core.model.FlowStatus;
import com.example.asynctaskflow.core.model.NodeStatus;
import com.example.asynctaskflow.core.repository.FlowDefinitionRepository;
import com.example.asynctaskflow.core.repository.FlowInstanceRepository;
import com.example.asynctaskflow.core.repository.FlowNodeInstanceRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FlowEngine {

    private final FlowDefinitionRepository flowDefinitionRepository;
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    private final PayloadCodec payloadCodec;
    private final FlowNodeExecutor flowNodeExecutor;
    private final Clock clock;

    public FlowEngine(FlowDefinitionRepository flowDefinitionRepository,
                      FlowInstanceRepository flowInstanceRepository,
                      FlowNodeInstanceRepository flowNodeInstanceRepository,
                      PayloadCodec payloadCodec,
                      FlowNodeExecutor flowNodeExecutor,
                      Clock clock) {
        this.flowDefinitionRepository = flowDefinitionRepository;
        this.flowInstanceRepository = flowInstanceRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        this.payloadCodec = payloadCodec;
        this.flowNodeExecutor = flowNodeExecutor;
        this.clock = clock;
    }

    @Transactional
    public FlowInstance startFlow(Long flowDefinitionId,
                                  Map<String, Object> flowPayload,
                                  String externalReference) {
        FlowDefinition definition = flowDefinitionRepository.findById(flowDefinitionId)
                .orElseThrow(() -> new EmptyResultDataAccessException("Flow definition not found: " + flowDefinitionId, 1));
        return startFlow(definition, flowPayload, externalReference);
    }

    @Transactional
    public FlowInstance startFlow(String flowCode,
                                  Integer version,
                                  Map<String, Object> flowPayload,
                                  String externalReference) {
        FlowDefinition definition = findDefinition(flowCode, version);
        return startFlow(definition, flowPayload, externalReference);
    }

    private FlowDefinition findDefinition(String flowCode, Integer version) {
        if (!StringUtils.hasText(flowCode)) {
            throw new IllegalArgumentException("flowCode must not be blank");
        }
        Optional<FlowDefinition> definition;
        if (version == null) {
            definition = flowDefinitionRepository.findFirstByCodeOrderByVersionDesc(flowCode);
        } else {
            definition = flowDefinitionRepository.findByCodeAndVersion(flowCode, version);
        }
        return definition.orElseThrow(() -> new EmptyResultDataAccessException(
                "Flow definition not found for code=%s version=%s".formatted(flowCode, version), 1));
    }

    private FlowInstance startFlow(FlowDefinition definition,
                                   Map<String, Object> flowPayload,
                                   String externalReference) {
        ensureNoActiveInstance(definition.getId());
        if (CollectionUtils.isEmpty(definition.getNodes())) {
            throw new IllegalStateException("Flow definition has no nodes configured");
        }

        Instant now = clock.instant();
        FlowInstance instance = new FlowInstance();
        instance.setFlowDefinition(definition);
        instance.setStatus(FlowStatus.RUNNING);
        instance.setPayloadJson(payloadCodec.toJson(flowPayload));
        instance.setExternalReference(externalReference);
        instance.setStartedAt(now);
        instance.setLockedAt(now);
        flowInstanceRepository.save(instance);

        FlowNodeDefinition firstNode = definition.getNodes().stream()
                .min(Comparator.comparingInt(FlowNodeDefinition::getSequence))
                .orElseThrow(() -> new IllegalStateException("Flow definition has no nodes"));

        FlowNodeInstance nodeInstance = new FlowNodeInstance();
        nodeInstance.setFlowInstance(instance);
        nodeInstance.setNodeDefinition(firstNode);
        nodeInstance.setStatus(NodeStatus.PENDING);
        nodeInstance.setNextAttemptAt(now);
        flowNodeInstanceRepository.save(nodeInstance);
        flowNodeInstanceRepository.flush();

        flowNodeExecutor.scheduleAfterCommit(nodeInstance.getId());
        return instance;
    }

    private void ensureNoActiveInstance(Long flowDefinitionId) {
        List<FlowStatus> activeStatuses = List.of(FlowStatus.PENDING, FlowStatus.RUNNING);
        if (flowInstanceRepository.existsByFlowDefinitionIdAndStatusIn(flowDefinitionId, activeStatuses)) {
            throw new IllegalStateException("Flow already has an active instance running");
        }
    }

    @Transactional
    public void triggerNode(Long nodeInstanceId) {
        FlowNodeInstance nodeInstance = flowNodeInstanceRepository.findById(nodeInstanceId)
                .orElseThrow(() -> new EmptyResultDataAccessException("Node instance not found: " + nodeInstanceId, 1));
        if (!nodeInstance.getNodeDefinition().isAllowManualTrigger()) {
            throw new IllegalStateException("Manual trigger disabled for node: " + nodeInstanceId);
        }
        nodeInstance.setStatus(NodeStatus.PENDING);
        nodeInstance.setNextAttemptAt(clock.instant());
        nodeInstance.setLockedAt(null);
        flowNodeExecutor.scheduleAfterCommit(nodeInstanceId);
    }

    @Transactional(readOnly = true)
    public FlowInstance getFlowInstance(Long flowInstanceId) {
        return flowInstanceRepository.findById(flowInstanceId)
                .orElseThrow(() -> new EmptyResultDataAccessException("Flow instance not found: " + flowInstanceId, 1));
    }

    @Transactional(readOnly = true)
    public List<FlowNodeInstance> getNodeInstances(Long flowInstanceId) {
        return flowNodeInstanceRepository.findByFlowInstanceIdOrderByCreatedAt(flowInstanceId);
    }
}
