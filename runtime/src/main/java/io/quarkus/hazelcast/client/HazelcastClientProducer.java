package io.quarkus.hazelcast.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class HazelcastClientProducer {
    private final AtomicReference<HazelcastInstance> instance = new AtomicReference<>(null);

    private HazelcastClientConfig hazelcastClientConfig;

    @Produces
    public HazelcastInstance instance() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress(hazelcastClientConfig.clusterAddress.split(","));
        hazelcastClientConfig.groupName
          .filter(s -> !s.isEmpty())
          .ifPresent(groupName -> clientConfig.getGroupConfig().setName(groupName));

        HazelcastInstance instance = HazelcastClient.newHazelcastClient(clientConfig);
        this.instance.set(instance);
        return instance;
    }

    @PreDestroy
    public void destroy() {
        HazelcastInstance hazelcastInstance = instance.get();
        if (hazelcastInstance != null) {
            hazelcastInstance.shutdown();
        }
    }

    public void setHazelcastClientConfig(HazelcastClientConfig hazelcastClientConfig) {
        this.hazelcastClientConfig = hazelcastClientConfig;
    }
}
