package com.example.asynctaskflow.core.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "flow_definitions",
        uniqueConstraints = @UniqueConstraint(name = "uk_flow_definition_code_version", columnNames = {"code", "version"}))
public class FlowDefinition extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private int version;

    @Column(length = 1000)
    private String description;

    @Version
    private Long entityVersion;

    @OneToMany(mappedBy = "flowDefinition", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequence ASC")
    private List<FlowNodeDefinition> nodes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<FlowNodeDefinition> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public void addNode(FlowNodeDefinition node) {
        node.setFlowDefinition(this);
        this.nodes.add(node);
    }

    public void removeNode(FlowNodeDefinition node) {
        node.setFlowDefinition(null);
        this.nodes.remove(node);
    }
}
