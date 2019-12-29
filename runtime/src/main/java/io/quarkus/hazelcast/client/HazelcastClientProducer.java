package io.quarkus.hazelcast.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientClasspathXmlConfig;
import com.hazelcast.client.config.ClientClasspathYamlConfig;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import io.quarkus.arc.DefaultBean;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@ApplicationScoped
public class HazelcastClientProducer {
    private final AtomicReference<HazelcastInstance> instance = new AtomicReference<>(null);

    private HazelcastClientConfig hazelcastClientConfig;

    @Produces
    @Singleton
    @DefaultBean
    public ClientConfig hazelcastConfigClientInstance() {
        return hazelcastClientConfig.configSource
          .map(toHazelcastClientConfig())
          .orElseGet(fromApplicationProperties());
    }

    private Supplier<ClientConfig> fromApplicationProperties() {
        return () -> {
            ClientConfig clientConfig = new ClientConfig();

            setClusterAddress(clientConfig);
            setGroupName(clientConfig);
            setLabels(clientConfig);

            setOutboundPorts(clientConfig);
            setOutboundPortDefinitions(clientConfig);

            setConnectionTimeout(clientConfig);
            setConnectionAttemptLimit(clientConfig);
            setConnectionAttemptPeriod(clientConfig);

            setExecutorPoolSize(clientConfig);

            return clientConfig;
        };
    }

    @Produces
    @Singleton
    @DefaultBean
    public HazelcastInstance hazelcastClientInstance(ClientConfig config) {
        HazelcastInstance instance = HazelcastClient.newHazelcastClient(config);
        this.instance.set(instance);
        return instance;
    }

    private static Function<String, ClientConfig> toHazelcastClientConfig() {
        return source -> {
            switch (source) {
                case "yaml":
                    return new ClientClasspathYamlConfig("hazelcast.yml");
                case "xml":
                    return new ClientClasspathXmlConfig("hazelcast.xml");
                default:
                    throw new IllegalStateException(String.format("Config source: [%s] is not supported", source));
            }
        };
    }

    private void setExecutorPoolSize(ClientConfig clientConfig) {
        hazelcastClientConfig.executorPoolSize
          .ifPresent(clientConfig::setExecutorPoolSize);
    }

    private void setClusterAddress(ClientConfig clientConfig) {
        clientConfig.getNetworkConfig().addAddress(hazelcastClientConfig.clusterMembers.split(","));
    }

    private void setGroupName(ClientConfig clientConfig) {
        hazelcastClientConfig.groupName
          .filter(s -> !s.isEmpty())
          .ifPresent(groupName -> clientConfig.getGroupConfig().setName(groupName));
    }

    private void setLabels(ClientConfig clientConfig) {
        hazelcastClientConfig.labels
          .map(s -> Arrays.stream(s.split(","))).orElseGet(Stream::empty)
          .forEach(clientConfig::addLabel);
    }

    private void setConnectionAttemptPeriod(ClientConfig clientConfig) {
        hazelcastClientConfig.connectionAttemptPeriod
          .ifPresent(period -> clientConfig.getNetworkConfig().setConnectionAttemptPeriod(period));
    }

    private void setConnectionAttemptLimit(ClientConfig clientConfig) {
        hazelcastClientConfig.connectionAttemptLimit
          .ifPresent(attempts -> clientConfig.getNetworkConfig().setConnectionAttemptLimit(attempts));
    }

    private void setConnectionTimeout(ClientConfig clientConfig) {
        hazelcastClientConfig.connectionTimeout
          .ifPresent(timeout -> clientConfig.getNetworkConfig().setConnectionTimeout(timeout));
    }

    private void setOutboundPortDefinitions(ClientConfig clientConfig) {
        hazelcastClientConfig.outboundPortDefinitions
          .map(str -> Arrays.stream(str.split(","))).orElseGet(Stream::empty)
          .forEach(definition -> clientConfig.getNetworkConfig().addOutboundPortDefinition(definition));
    }

    private void setOutboundPorts(ClientConfig clientConfig) {
        hazelcastClientConfig.outboundPorts
          .map(str -> Arrays.stream(str.split(","))).orElseGet(Stream::empty)
          .forEach(port -> clientConfig.getNetworkConfig().addOutboundPort(Integer.parseInt(port)));
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
