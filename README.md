# Async Task Flow Spring Boot Starter

A Spring Boot starter that provides a chained asynchronous task execution engine backed by MySQL. Each flow consists of ordered nodes that run sequentially with persistence, retry, manual triggering, and recovery via scheduled polling.

## Features

- Flow and node metadata persisted with Spring Data JPA (MySQL 8.0+).
- Sequential asynchronous execution with a configurable thread pool.
- Node handler registry so each node type can be executed by custom logic.
- Manual node triggering REST endpoints and automatic recovery cron job for long-pending tasks.
- Auto-configuration, configuration properties, and defaults suitable for starter usage.

## Getting Started

1. Add the starter dependency to your project:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>async-task-flow-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

2. Configure your datasource (MySQL 8.0 or compatible) and let Flyway run the bundled migrations (under `db/migration`) or integrate the SQL into your existing migration pipeline.

3. Implement one or more `FlowNodeHandler` beans to handle custom node types:

```java
@Component
public class EmailNodeHandler implements FlowNodeHandler {

    @Override
    public String getType() {
        return "EMAIL";
    }

    @Override
    public NodeExecutionResult execute(NodeExecutionContext context) {
        // Send email based on context.getNodeConfiguration() / context.getFlowPayload()
        return NodeExecutionResult.complete();
    }
}
```

4. Ensure scheduling is enabled (e.g. `@EnableScheduling`) so the recovery cron job runs.

5. Trigger flows via the provided REST API or invoke the `FlowEngine` bean directly.

## REST API

The starter exposes a lightweight controller under `/async-flow`:

- `POST /async-flow/instances` — starts a flow by code and optional version.
- `POST /async-flow/nodes/{nodeId}/trigger` — manually triggers a specific node instance.
- `GET /async-flow/instances/{instanceId}` — retrieves the flow instance and node statuses.

Request body for starting a flow:

```json
{
  "flowCode": "user-onboarding",
  "version": 1,
  "externalReference": "user-123",
  "payload": { "userId": 123 }
}
```

## Configuration Properties

```yaml
async:
  taskflow:
    enabled: true
    scheduler:
      pending-cron: "0 */1 * * * *"   # Cron for picking up pending nodes
      stalled-after: 5m                # How long before a RUNNING node is considered stalled
      max-batch-size: 25               # Max nodes recovered per scheduler run
    executor:
      core-pool-size: 4
      max-pool-size: 16
      queue-capacity: 100
      keep-alive: 60s
```

All properties are optional and have sensible defaults.

## Programmatic Usage

You can interact with the engine directly via the `FlowEngine` bean:

```java
@Autowired
private FlowEngine flowEngine;

public void launch() {
    FlowInstance instance = flowEngine.startFlow("user-onboarding", 1, Map.of("userId", 123L), "user-123");
}
```

Manual triggers can also be performed via the same bean (`flowEngine.triggerNode(nodeId)`).

## Notes

- Only one active (pending or running) instance per flow definition is allowed at a time.
- The engine expects node definitions to exist in the database and to reference registered handlers via the `type` field.
- Flyway migration `V1__create_async_task_flow_tables.sql` creates the required tables; add follow-up migrations for your custom data.
- A local JDK 17 installation is required to build the project (`mvn compile`).
