package io.quarkus.hazelcast.client;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * @author Grzegorz Piwowarek
 */
@ConfigRoot(name = "hazelcast-client", phase = ConfigPhase.RUN_TIME)
public class HazelcastClientConfig {

    /**
     * Hazelcast Cluster address
     */
    @ConfigItem
    public Optional<String> clusterMembers;

    /**
     * Hazelcast client label name
     */
    @ConfigItem
    public Optional<String> labels;

    /**
     * Hazelcast Cluster group name
     */
    @ConfigItem
    public Optional<String> groupName;

    /**
     * Outbound port
     */
    @ConfigItem
    public Optional<String> outboundPorts;

    /**
     * Outbound port definition
     */
    @ConfigItem
    public Optional<String> outboundPortDefinitions;

    /**
     * Connection attempt limit
     */
    @ConfigItem
    public Optional<Integer> connectionAttemptLimit;

    /**
     * Connection attempt period
     */
    @ConfigItem
    public Optional<Integer> connectionAttemptPeriod;

    /**
     * Connection timeout
     */
    @ConfigItem
    public OptionalInt connectionTimeout;

    /**
     * Executor pool size
     */
    @ConfigItem
    public OptionalInt executorPoolSize;
}
