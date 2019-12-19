package io.quarkus.hazelcast.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class HazelcastClientProducer {
    private HazelcastClientConfig hazelcastClientConfig;

    @Produces
    public HazelcastInstance instance() {
        ClientConfig clientConfig = new ClientConfig();
        System.out.println("hz_config: " + hazelcastClientConfig.clusterAddress);
        clientConfig.getNetworkConfig().addAddress(hazelcastClientConfig.clusterAddress.split(","));
        return HazelcastClient.newHazelcastClient(clientConfig);
    }

    public void setHazelcastClientConfig(HazelcastClientConfig hazelcastClientConfig) {
        this.hazelcastClientConfig = hazelcastClientConfig;
    }
}
