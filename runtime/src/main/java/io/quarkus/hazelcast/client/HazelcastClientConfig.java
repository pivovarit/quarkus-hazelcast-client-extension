package io.quarkus.hazelcast.client;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "hazelcast", phase = ConfigPhase.RUN_TIME)
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
    public String groupName;

    /**
     * Hazelcast Cluster group password
     */
    @ConfigItem
    public String groupPassword;
}
