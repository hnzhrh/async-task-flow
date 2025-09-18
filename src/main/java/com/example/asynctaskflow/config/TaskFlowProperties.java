package com.example.asynctaskflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "async.taskflow")
public class TaskFlowProperties {

    /** Whether the async task flow engine is enabled. */
    private boolean enabled = true;

    private final Scheduler scheduler = new Scheduler();

    private final Executor executor = new Executor();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public Executor getExecutor() {
        return executor;
    }

    public static class Scheduler {
        /** Cron expression used to pick up pending nodes. */
        private String pendingCron = "0 */1 * * * *";
        /** Consider running nodes stalled after this duration. */
        private Duration stalledAfter = Duration.ofMinutes(5);
        /** Maximum number of nodes to recover in a single scheduler run. */
        private int maxBatchSize = 25;

        public String getPendingCron() {
            return pendingCron;
        }

        public void setPendingCron(String pendingCron) {
            this.pendingCron = pendingCron;
        }

        public Duration getStalledAfter() {
            return stalledAfter;
        }

        public void setStalledAfter(Duration stalledAfter) {
            this.stalledAfter = stalledAfter;
        }

        public int getMaxBatchSize() {
            return maxBatchSize;
        }

        public void setMaxBatchSize(int maxBatchSize) {
            this.maxBatchSize = maxBatchSize;
        }
    }

    public static class Executor {
        private int corePoolSize = 4;
        private int maxPoolSize = 16;
        private int queueCapacity = 100;
        private Duration keepAlive = Duration.ofSeconds(60);

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        public Duration getKeepAlive() {
            return keepAlive;
        }

        public void setKeepAlive(Duration keepAlive) {
            this.keepAlive = keepAlive;
        }
    }
}
