package com.example.asynctaskflow.core.service;

import com.example.asynctaskflow.core.handler.FlowNodeHandler;
import com.example.asynctaskflow.core.handler.FlowNodeHandlerRegistry;
import com.example.asynctaskflow.core.handler.NodeExecutionContext;
import com.example.asynctaskflow.core.handler.NodeExecutionResult;
import com.example.asynctaskflow.core.model.FlowDefinition;
import com.example.asynctaskflow.core.model.FlowInstance;
import com.example.asynctaskflow.core.model.FlowNodeDefinition;
import com.example.asynctaskflow.core.model.FlowNodeInstance;
import com.example.asynctaskflow.core.model.FlowStatus;
import com.example.asynctaskflow.core.model.NodeStatus;
import com.example.asynctaskflow.core.repository.FlowNodeInstanceRepository;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

public class FlowNodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(FlowNodeExecutor.class);

    private final FlowNodeInstanceRepository nodeInstanceRepository;
    private final FlowNodeHandlerRegistry handlerRegistry;
    private final PayloadCodec payloadCodec;
    private final Executor taskExecutor;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public FlowNodeExecutor(FlowNodeInstanceRepository nodeInstanceRepository,
                            FlowNodeHandlerRegistry handlerRegistry,
                            PayloadCodec payloadCodec,
                            Executor taskExecutor,
                            PlatformTransactionManager transactionManager,
                            Clock clock) {
        this.nodeInstanceRepository = nodeInstanceRepository;
        this.handlerRegistry = handlerRegistry;
        this.payloadCodec = payloadCodec;
        this.taskExecutor = taskExecutor;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.clock = clock;
    }

    public void scheduleAfterCommit(Long nodeInstanceId) {
        if (nodeInstanceId == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    executeAsync(nodeInstanceId);
                }
            });
        } else {
            executeAsync(nodeInstanceId);
        }
    }

    public void executeAsync(Long nodeInstanceId) {
        taskExecutor.execute(() -> executeSync(nodeInstanceId));
    }

    public void executeSync(Long nodeInstanceId) {
        List<Long> chainedNodes = transactionTemplate.execute(status -> executeNodeInternal(nodeInstanceId));
        if (!CollectionUtils.isEmpty(chainedNodes)) {
            chainedNodes.forEach(this::executeAsync);
        }
    }

    private List<Long> executeNodeInternal(Long nodeInstanceId) {
        FlowNodeInstance nodeInstance = nodeInstanceRepository.findByIdForUpdate(nodeInstanceId).orElse(null);
        if (nodeInstance == null) {
            log.debug("Node instance {} not found", nodeInstanceId);
            return List.of();
        }

        if (!isExecutable(nodeInstance.getStatus())) {
            log.debug("Node instance {} in status {} cannot be executed", nodeInstanceId, nodeInstance.getStatus());
            return List.of();
        }

        FlowInstance flowInstance = nodeInstance.getFlowInstance();
        FlowDefinition flowDefinition = nodeInstance.getNodeDefinition().getFlowDefinition();

        Instant now = clock.instant();
        nodeInstance.setStatus(NodeStatus.RUNNING);
        nodeInstance.setStartedAt(now);
        nodeInstance.setAttemptCount(nodeInstance.getAttemptCount() + 1);
        nodeInstance.setLockedAt(now);
        nodeInstance.setNextAttemptAt(null);
        nodeInstance.setLastError(null);

        if (flowInstance.getStartedAt() == null) {
            flowInstance.setStartedAt(now);
        }
        flowInstance.setStatus(FlowStatus.RUNNING);
        flowInstance.setLockedAt(now);
        flowInstance.setLastError(null);

        FlowNodeHandler handler = resolveHandler(nodeInstance);
        if (handler == null) {
            markNodeFailed(nodeInstance, flowInstance, now, "No handler registered for node type %s".formatted(nodeInstance.getNodeDefinition().getType()));
            return List.of();
        }

        Map<String, Object> flowPayload = new HashMap<>(payloadCodec.toMap(flowInstance.getPayloadJson()));
        Map<String, Object> nodePayload = new HashMap<>(payloadCodec.toMap(nodeInstance.getContextJson()));
        Map<String, Object> nodeConfig = new HashMap<>(payloadCodec.toMap(nodeInstance.getNodeDefinition().getConfigurationJson()));

        NodeExecutionContext context = new NodeExecutionContext(flowDefinition, flowInstance,
                nodeInstance.getNodeDefinition(), nodeInstance, flowPayload, nodePayload, nodeConfig);

        try {
            NodeExecutionResult result = handler.execute(context);
            return handleResult(result, nodeInstance, flowInstance, flowPayload, nodePayload, now);
        } catch (Exception ex) {
            log.error("Node execution failed for nodeInstance {}", nodeInstanceId, ex);
            markNodeFailed(nodeInstance, flowInstance, now, ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
            return List.of();
        }
    }

    private boolean isExecutable(NodeStatus status) {
        return status == NodeStatus.PENDING || status == NodeStatus.WAITING;
    }

    @Nullable
    private FlowNodeHandler resolveHandler(FlowNodeInstance nodeInstance) {
        String type = nodeInstance.getNodeDefinition().getType();
        return handlerRegistry.getHandler(type);
    }

    private List<Long> handleResult(NodeExecutionResult result,
                                    FlowNodeInstance nodeInstance,
                                    FlowInstance flowInstance,
                                    Map<String, Object> flowPayload,
                                    Map<String, Object> nodePayload,
                                    Instant now) {
        if (result == null) {
            markNodeFailed(nodeInstance, flowInstance, now, "Handler returned null result");
            return List.of();
        }

        switch (result.getStatus()) {
            case COMPLETE -> {
                applyPayloadUpdates(flowInstance, nodeInstance, flowPayload, nodePayload, result);
                nodeInstance.setStatus(NodeStatus.COMPLETED);
                nodeInstance.setCompletedAt(now);
                nodeInstance.setLockedAt(null);
                return scheduleNext(nodeInstance, flowInstance, now);
            }
            case RETRY -> {
                Duration delay = result.getRetryDelay();
                Instant nextAttempt = delay != null ? now.plus(delay) : now.plus(Duration.ofMinutes(1));
                nodeInstance.setStatus(NodeStatus.PENDING);
                nodeInstance.setNextAttemptAt(nextAttempt);
                nodeInstance.setLockedAt(null);
                nodeInstance.setLastError(result.getMessage());
                flowInstance.setLockedAt(null);
                return List.of();
            }
            case FAILED -> {
                String message = result.getMessage() != null ? result.getMessage() : "Node reported failure";
                markNodeFailed(nodeInstance, flowInstance, now, message);
                return List.of();
            }
            default -> {
                markNodeFailed(nodeInstance, flowInstance, now, "Unhandled node execution status");
                return List.of();
            }
        }
    }

    private void applyPayloadUpdates(FlowInstance flowInstance,
                                     FlowNodeInstance nodeInstance,
                                     Map<String, Object> flowPayload,
                                     Map<String, Object> nodePayload,
                                     NodeExecutionResult result) {
        merge(flowPayload, result.getFlowPayloadUpdates());
        merge(nodePayload, result.getNodePayloadUpdates());
        flowInstance.setPayloadJson(payloadCodec.toJson(flowPayload));
        nodeInstance.setContextJson(payloadCodec.toJson(nodePayload));
    }

    private List<Long> scheduleNext(FlowNodeInstance currentNode,
                                    FlowInstance flowInstance,
                                    Instant now) {
        Optional<FlowNodeDefinition> nextNodeDefinition = findNextNode(currentNode.getNodeDefinition());
        if (nextNodeDefinition.isEmpty()) {
            flowInstance.setStatus(FlowStatus.COMPLETED);
            flowInstance.setCompletedAt(now);
            flowInstance.setLockedAt(null);
            return List.of();
        }

        FlowNodeInstance nextInstance = new FlowNodeInstance();
        nextInstance.setFlowInstance(flowInstance);
        nextInstance.setNodeDefinition(nextNodeDefinition.get());
        nextInstance.setStatus(NodeStatus.PENDING);
        nextInstance.setNextAttemptAt(now);
        nextInstance.setLockedAt(null);
        nodeInstanceRepository.save(nextInstance);
        nodeInstanceRepository.flush();
        return List.of(nextInstance.getId());
    }

    private Optional<FlowNodeDefinition> findNextNode(FlowNodeDefinition current) {
        FlowDefinition flowDefinition = current.getFlowDefinition();
        return flowDefinition.getNodes().stream()
                .filter(node -> node.getSequence() > current.getSequence())
                .min(Comparator.comparingInt(FlowNodeDefinition::getSequence));
    }

    private void markNodeFailed(FlowNodeInstance nodeInstance,
                                FlowInstance flowInstance,
                                Instant now,
                                String message) {
        nodeInstance.setStatus(NodeStatus.FAILED);
        nodeInstance.setCompletedAt(now);
        nodeInstance.setLockedAt(null);
        nodeInstance.setLastError(message);

        flowInstance.setStatus(FlowStatus.FAILED);
        flowInstance.setCompletedAt(now);
        flowInstance.setLockedAt(null);
        flowInstance.setLastError(message);
    }

    private void merge(Map<String, Object> base, Map<String, Object> updates) {
        if (base == null || updates == null || updates.isEmpty()) {
            return;
        }
        base.putAll(updates);
    }
}
