package io.quarkus.hazelcast.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import io.quarkus.arc.DefaultBean;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@ApplicationScoped
public class HazelcastClientProducer {
    private final AtomicReference<HazelcastInstance> instance = new AtomicReference<>(null);

    private HazelcastClientConfig hazelcastClientConfig;

    @Produces
    @DefaultBean
    public ClientConfig hazelcastConfigClientInstance() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress(hazelcastClientConfig.clusterAddress.split(","));

        hazelcastClientConfig.groupName
          .filter(s -> !s.isEmpty())
          .ifPresent(groupName -> clientConfig.getGroupConfig().setName(groupName));

        hazelcastClientConfig.outboundPorts
          .map(str -> Arrays.stream(str.split(","))).orElseGet(Stream::empty)
          .forEach(port -> clientConfig.getNetworkConfig().addOutboundPort(Integer.parseInt(port)));

        hazelcastClientConfig.outboundPortDefinitions
          .map(str -> Arrays.stream(str.split(","))).orElseGet(Stream::empty)
          .forEach(definition -> clientConfig.getNetworkConfig().addOutboundPortDefinition(definition));

        hazelcastClientConfig.connectionTimeout
          .ifPresent(timeout -> clientConfig.getNetworkConfig().setConnectionTimeout(timeout));

        hazelcastClientConfig.connectionAttemptLimit
          .ifPresent(attempts -> clientConfig.getNetworkConfig().setConnectionAttemptLimit(attempts));

        hazelcastClientConfig.connectionAttemptPeriod
          .ifPresent(period -> clientConfig.getNetworkConfig().setConnectionAttemptPeriod(period));

        return clientConfig;
    }

    @Produces
    @DefaultBean
    public HazelcastInstance hazelcastClientInstance(ClientConfig config) {
        HazelcastInstance instance = HazelcastClient.newHazelcastClient(config);
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
