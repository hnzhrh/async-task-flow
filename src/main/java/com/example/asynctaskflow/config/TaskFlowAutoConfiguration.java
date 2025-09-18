package com.example.asynctaskflow.config;

import com.example.asynctaskflow.core.handler.FlowNodeHandler;
import com.example.asynctaskflow.core.handler.FlowNodeHandlerRegistry;
import com.example.asynctaskflow.core.repository.FlowDefinitionRepository;
import com.example.asynctaskflow.core.repository.FlowInstanceRepository;
import com.example.asynctaskflow.core.repository.FlowNodeInstanceRepository;
import com.example.asynctaskflow.core.service.FlowEngine;
import com.example.asynctaskflow.core.service.FlowNodeExecutor;
import com.example.asynctaskflow.core.service.PayloadCodec;
import com.example.asynctaskflow.scheduling.PendingNodeScheduler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.Executor;

@AutoConfiguration(after = DataSourceAutoConfiguration.class)
@ConditionalOnClass({DataSource.class})
@EnableConfigurationProperties(TaskFlowProperties.class)
@ConditionalOnProperty(prefix = "async.taskflow", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TaskFlowAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Clock taskFlowClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    public PayloadCodec taskFlowPayloadCodec(ObjectMapper objectMapper) {
        return new PayloadCodec(objectMapper);
    }

    @Bean(name = "taskFlowExecutor")
    @ConditionalOnMissingBean(name = "taskFlowExecutor")
    public Executor taskFlowExecutor(TaskFlowProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("task-flow-");
        executor.setCorePoolSize(properties.getExecutor().getCorePoolSize());
        executor.setMaxPoolSize(properties.getExecutor().getMaxPoolSize());
        executor.setQueueCapacity(properties.getExecutor().getQueueCapacity());
        executor.setKeepAliveSeconds((int) properties.getExecutor().getKeepAlive().getSeconds());
        executor.initialize();
        return executor;
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowNodeHandlerRegistry flowNodeHandlerRegistry(ObjectProvider<List<FlowNodeHandler>> handlersProvider) {
        List<FlowNodeHandler> handlers = handlersProvider.getIfAvailable(List::of);
        return new FlowNodeHandlerRegistry(handlers);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowNodeExecutor flowNodeExecutor(FlowNodeInstanceRepository nodeInstanceRepository,
                                             FlowNodeHandlerRegistry handlerRegistry,
                                             PayloadCodec payloadCodec,
                                             Executor taskFlowExecutor,
                                             PlatformTransactionManager transactionManager,
                                             Clock taskFlowClock) {
        return new FlowNodeExecutor(nodeInstanceRepository, handlerRegistry, payloadCodec,
                taskFlowExecutor, transactionManager, taskFlowClock);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowEngine flowEngine(FlowDefinitionRepository flowDefinitionRepository,
                                 FlowInstanceRepository flowInstanceRepository,
                                 FlowNodeInstanceRepository flowNodeInstanceRepository,
                                 PayloadCodec payloadCodec,
                                 FlowNodeExecutor flowNodeExecutor,
                                 Clock taskFlowClock) {
        return new FlowEngine(flowDefinitionRepository, flowInstanceRepository,
                flowNodeInstanceRepository, payloadCodec, flowNodeExecutor, taskFlowClock);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnSingleCandidate(PlatformTransactionManager.class)
    public PendingNodeScheduler pendingNodeScheduler(TaskFlowProperties properties,
                                                      FlowNodeInstanceRepository nodeInstanceRepository,
                                                      FlowNodeExecutor flowNodeExecutor,
                                                      PlatformTransactionManager transactionManager,
                                                      Clock taskFlowClock) {
        return new PendingNodeScheduler(properties, nodeInstanceRepository,
                flowNodeExecutor, transactionManager, taskFlowClock);
    }
}
