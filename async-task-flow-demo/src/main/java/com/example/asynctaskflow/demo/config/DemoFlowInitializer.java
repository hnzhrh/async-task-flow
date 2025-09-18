package com.example.asynctaskflow.demo.config;

import com.example.asynctaskflow.core.model.FlowDefinition;
import com.example.asynctaskflow.core.model.FlowNodeDefinition;
import com.example.asynctaskflow.core.repository.FlowDefinitionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class DemoFlowInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoFlowInitializer.class);
    public static final String DEMO_FLOW_CODE = "demo-welcome";

    private final FlowDefinitionRepository flowDefinitionRepository;
    private final ObjectMapper objectMapper;

    public DemoFlowInitializer(FlowDefinitionRepository flowDefinitionRepository,
                               ObjectMapper objectMapper) {
        this.flowDefinitionRepository = flowDefinitionRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (flowDefinitionRepository.findByCodeAndVersion(DEMO_FLOW_CODE, 1).isPresent()) {
            return;
        }
        log.info("Bootstrapping demo flow definition '{}'", DEMO_FLOW_CODE);
        FlowDefinition definition = new FlowDefinition();
        definition.setCode(DEMO_FLOW_CODE);
        definition.setName("Demo Welcome Flow");
        definition.setVersion(1);
        definition.setDescription("Sample flow seeded by the demo application");

        FlowNodeDefinition prepare = new FlowNodeDefinition();
        prepare.setSequence(1);
        prepare.setType("prepare-message");
        prepare.setDisplayName("Prepare greeting message");
        prepare.setConfigurationJson(writeJson(Map.of("greeting", "Hello")));

        FlowNodeDefinition notify = new FlowNodeDefinition();
        notify.setSequence(2);
        notify.setType("send-notification");
        notify.setDisplayName("Send notification");

        definition.addNode(prepare);
        definition.addNode(notify);

        flowDefinitionRepository.save(definition);
    }

    private String writeJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build configuration JSON", e);
        }
    }
}
