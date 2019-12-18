package com.hazelcast.quarkus;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "hazelcast", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public final class HazelcastConfig {

    @ConfigItem
    public String clusterAddress; // quarkus.hazelcast.cluster-address=192.168.1.1
}
