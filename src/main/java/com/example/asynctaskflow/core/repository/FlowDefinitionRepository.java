package com.example.asynctaskflow.core.repository;

import com.example.asynctaskflow.core.model.FlowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlowDefinitionRepository extends JpaRepository<FlowDefinition, Long> {

    Optional<FlowDefinition> findFirstByCodeOrderByVersionDesc(String code);

    Optional<FlowDefinition> findByCodeAndVersion(String code, int version);
}
