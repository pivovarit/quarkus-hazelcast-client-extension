package io.quarkus.hazelcast.client;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

import java.util.Optional;

@ConfigRoot(name = "hazelcast-client", phase = ConfigPhase.RUN_TIME)
public class HazelcastClientConfig {

    /**
     * Hazelcast Cluster address
     */
    @ConfigItem(defaultValue = "http://localhost:5701")
    public String clusterAddress;

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
    public Optional<Integer> connectionTimeout;

    /**
     * Executor pool size
     */
    @ConfigItem
    public Optional<Integer> executorPoolSize;
}
