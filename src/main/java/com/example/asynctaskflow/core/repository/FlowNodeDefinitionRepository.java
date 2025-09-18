package com.example.asynctaskflow.core.repository;

import com.example.asynctaskflow.core.model.FlowNodeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlowNodeDefinitionRepository extends JpaRepository<FlowNodeDefinition, Long> {
}
