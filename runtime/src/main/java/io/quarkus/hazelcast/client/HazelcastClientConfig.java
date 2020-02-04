package io.quarkus.hazelcast.client;

import java.net.Inet4Address;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "hazelcast-client", phase = ConfigPhase.RUN_TIME)
public class HazelcastClientConfig {

    /**
     * Hazelcast Cluster address
     */
    @ConfigItem
    public List<Inet4Address> clusterMembers;

    /**
     * Hazelcast client label name
     */
    @ConfigItem
    public List<String> labels;

    /**
     * Hazelcast Cluster group name
     */
    @ConfigItem
    public Optional<String> clusterName;

    /**
     * Outbound port
     */
    @ConfigItem
    public List<Integer> outboundPorts;

    /**
     * Outbound port definition
     */
    @ConfigItem
    public List<String> outboundPortDefinitions;

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
