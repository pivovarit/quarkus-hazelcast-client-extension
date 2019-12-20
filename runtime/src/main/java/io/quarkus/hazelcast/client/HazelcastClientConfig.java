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
    @ConfigItem
    public String clusterAddress;

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
}
