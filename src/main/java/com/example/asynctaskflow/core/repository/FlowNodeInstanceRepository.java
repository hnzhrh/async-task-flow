package com.example.asynctaskflow.core.repository;

import com.example.asynctaskflow.core.model.FlowNodeInstance;
import com.example.asynctaskflow.core.model.NodeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface FlowNodeInstanceRepository extends JpaRepository<FlowNodeInstance, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ni from FlowNodeInstance ni where ni.id = :id")
    Optional<FlowNodeInstance> findByIdForUpdate(Long id);

    @Query("select ni from FlowNodeInstance ni " +
           "where ni.status = com.example.asynctaskflow.core.model.NodeStatus.PENDING " +
           "and (ni.nextAttemptAt is null or ni.nextAttemptAt <= :now) " +
           "order by ni.createdAt asc")
    List<FlowNodeInstance> findPendingNodesReadyForExecution(Instant now);

    @Query("select ni from FlowNodeInstance ni " +
           "where ni.status = com.example.asynctaskflow.core.model.NodeStatus.RUNNING " +
           "and ni.lockedAt <= :staleBefore")
    List<FlowNodeInstance> findStalledRunningNodes(Instant staleBefore);

    List<FlowNodeInstance> findByFlowInstanceIdOrderByCreatedAt(Long flowInstanceId);

    List<FlowNodeInstance> findByFlowInstanceIdAndStatus(Long flowInstanceId, NodeStatus status);
}
