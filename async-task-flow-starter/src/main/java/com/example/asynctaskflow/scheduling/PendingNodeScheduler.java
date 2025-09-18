package com.example.asynctaskflow.scheduling;

import com.example.asynctaskflow.config.TaskFlowProperties;
import com.example.asynctaskflow.core.model.FlowNodeInstance;
import com.example.asynctaskflow.core.model.NodeStatus;
import com.example.asynctaskflow.core.repository.FlowNodeInstanceRepository;
import com.example.asynctaskflow.core.service.FlowNodeExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class PendingNodeScheduler {

    private static final Logger log = LoggerFactory.getLogger(PendingNodeScheduler.class);

    private final TaskFlowProperties properties;
    private final FlowNodeInstanceRepository nodeInstanceRepository;
    private final FlowNodeExecutor flowNodeExecutor;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public PendingNodeScheduler(TaskFlowProperties properties,
                                FlowNodeInstanceRepository nodeInstanceRepository,
                                FlowNodeExecutor flowNodeExecutor,
                                PlatformTransactionManager transactionManager,
                                Clock clock) {
        this.properties = properties;
        this.nodeInstanceRepository = nodeInstanceRepository;
        this.flowNodeExecutor = flowNodeExecutor;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.clock = clock;
    }

    @Scheduled(cron = "${async.taskflow.scheduler.pending-cron:0 */1 * * * *}")
    public void recoverPendingNodes() {
        if (!properties.isEnabled()) {
            return;
        }
        Instant now = clock.instant();
        int batchSize = Math.max(1, properties.getScheduler().getMaxBatchSize());

        List<FlowNodeInstance> pendingNodes = nodeInstanceRepository.findPendingNodesReadyForExecution(now);
        pendingNodes.stream()
                .limit(batchSize)
                .map(FlowNodeInstance::getId)
                .forEach(flowNodeExecutor::executeAsync);

        Duration stalledAfter = properties.getScheduler().getStalledAfter();
        if (stalledAfter != null && !stalledAfter.isNegative() && !stalledAfter.isZero()) {
            Instant staleBefore = now.minus(stalledAfter);
            List<FlowNodeInstance> stalledNodes = nodeInstanceRepository.findStalledRunningNodes(staleBefore);
            stalledNodes.stream()
                    .limit(batchSize)
                    .map(FlowNodeInstance::getId)
                    .forEach(this::resetAndRequeue);
            if (!stalledNodes.isEmpty()) {
                log.warn("Recovered {} stalled node(s)", stalledNodes.size());
            }
        }
    }

    private void resetAndRequeue(Long nodeInstanceId) {
        transactionTemplate.executeWithoutResult(status -> nodeInstanceRepository.findByIdForUpdate(nodeInstanceId).ifPresent(node -> {
            if (node.getStatus() == NodeStatus.RUNNING) {
                node.setStatus(NodeStatus.PENDING);
                node.setNextAttemptAt(clock.instant());
                node.setLockedAt(null);
                node.setLastError("Recovered by scheduler after stall");
            }
        }));
        flowNodeExecutor.executeAsync(nodeInstanceId);
    }
}
