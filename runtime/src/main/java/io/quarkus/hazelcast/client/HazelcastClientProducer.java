package io.quarkus.hazelcast.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class HazelcastClientProducer {
    private volatile HazelcastInstance instance;

    private HazelcastClientConfig hazelcastClientConfig;

    @Produces
    public HazelcastInstance instance() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress(hazelcastClientConfig.clusterAddress.split(","));
        hazelcastClientConfig.groupName
          .filter(s -> !s.isEmpty())
          .ifPresent(groupName -> clientConfig.getGroupConfig().setName(groupName));

        instance = HazelcastClient.newHazelcastClient(clientConfig);

        return instance;
    }

    @PreDestroy
    public void destroy() {
        if (instance != null) {
            instance.shutdown();
        }
    }

    public void setHazelcastClientConfig(HazelcastClientConfig hazelcastClientConfig) {
        this.hazelcastClientConfig = hazelcastClientConfig;
    }
}
