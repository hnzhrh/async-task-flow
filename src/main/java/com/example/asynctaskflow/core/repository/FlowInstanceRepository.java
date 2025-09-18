package com.example.asynctaskflow.core.repository;

import com.example.asynctaskflow.core.model.FlowInstance;
import com.example.asynctaskflow.core.model.FlowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FlowInstanceRepository extends JpaRepository<FlowInstance, Long> {

    boolean existsByFlowDefinitionIdAndStatusIn(Long flowDefinitionId, Collection<FlowStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select fi from FlowInstance fi where fi.id = :id")
    Optional<FlowInstance> findByIdForUpdate(Long id);

    List<FlowInstance> findByFlowDefinitionIdOrderByCreatedAtDesc(Long flowDefinitionId);
}
