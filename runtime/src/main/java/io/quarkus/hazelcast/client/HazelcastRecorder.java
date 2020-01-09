package io.quarkus.hazelcast.client;

import com.hazelcast.client.config.ClientClasspathXmlConfig;
import com.hazelcast.client.config.ClientClasspathYamlConfig;
import com.hazelcast.client.config.ClientConfig;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Stream;

@Recorder
public class HazelcastRecorder {
    private static final String CONFIG_FILENAME = "hazelcast-client";

    public void configureRuntimeProperties(HazelcastClientConfig config) {
        HazelcastClientProducer hazelcastClientProducer = Arc.container().instance(HazelcastClientProducer.class).get();
        hazelcastClientProducer.setClientConfig(resolveClientConfig(config));
    }

    private boolean exists(String s) {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(s)) {
            return stream != null;
        } catch (IOException e) {
            return false;
        }
    }

    private ClientConfig resolveClientConfig(HazelcastClientConfig config) {
        boolean yml = exists(CONFIG_FILENAME + ".yml");
        boolean yaml = exists(CONFIG_FILENAME + ".yaml");
        boolean xml = exists(CONFIG_FILENAME + ".xml");

        if (Stream.of(yml, yaml, xml).mapToInt(b -> b ? 1 : 0).sum() > 1) {
            throw new RuntimeException("max one configuration file is supported");
        }

        if (yml) {
            return new ClientClasspathYamlConfig(CONFIG_FILENAME + ".yml");
        } else if (yaml) {
            return new ClientClasspathYamlConfig(CONFIG_FILENAME + ".yaml");
        } else if (xml) {
            return new ClientClasspathXmlConfig(CONFIG_FILENAME + ".xml");
        }
        return fromApplicationProperties(config);
    }

    private ClientConfig fromApplicationProperties(HazelcastClientConfig config) {
        ClientConfig clientConfig = new ClientConfig();

        setClusterAddress(clientConfig, config);
        setGroupName(clientConfig, config);
        setLabels(clientConfig, config);

        setOutboundPorts(clientConfig, config);
        setOutboundPortDefinitions(clientConfig, config);

        setConnectionTimeout(clientConfig, config);
        setConnectionAttemptLimit(clientConfig, config);
        setConnectionAttemptPeriod(clientConfig, config);

        setExecutorPoolSize(clientConfig, config);

        return clientConfig;
    }

    private void setClusterAddress(ClientConfig clientConfig, HazelcastClientConfig config) {
        clientConfig.getNetworkConfig().addAddress(config.clusterMembers.split(","));
    }

    private void setGroupName(ClientConfig clientConfig, HazelcastClientConfig config) {
        config.groupName
          .filter(s -> !s.isEmpty())
          .ifPresent(groupName -> clientConfig.getGroupConfig().setName(groupName));
    }

    private void setLabels(ClientConfig clientConfig, HazelcastClientConfig config) {
        config.labels
          .map(s -> Arrays.stream(s.split(","))).orElseGet(Stream::empty)
          .forEach(clientConfig::addLabel);
    }

    private void setConnectionAttemptPeriod(ClientConfig clientConfig, HazelcastClientConfig config) {
        config.connectionAttemptPeriod
          .ifPresent(period -> clientConfig.getNetworkConfig().setConnectionAttemptPeriod(period));
    }

    private void setConnectionAttemptLimit(ClientConfig clientConfig, HazelcastClientConfig config) {
        config.connectionAttemptLimit
          .ifPresent(attempts -> clientConfig.getNetworkConfig().setConnectionAttemptLimit(attempts));
    }

    private void setConnectionTimeout(ClientConfig clientConfig, HazelcastClientConfig config) {
        config.connectionTimeout
          .ifPresent(timeout -> clientConfig.getNetworkConfig().setConnectionTimeout(timeout));
    }

    private void setOutboundPortDefinitions(ClientConfig clientConfig, HazelcastClientConfig config) {
        config.outboundPortDefinitions
          .map(str -> Arrays.stream(str.split(","))).orElseGet(Stream::empty)
          .forEach(definition -> clientConfig.getNetworkConfig().addOutboundPortDefinition(definition));
    }

    private void setOutboundPorts(ClientConfig clientConfig, HazelcastClientConfig config) {
        config.outboundPorts
          .map(str -> Arrays.stream(str.split(","))).orElseGet(Stream::empty)
          .forEach(port -> clientConfig.getNetworkConfig().addOutboundPort(Integer.parseInt(port)));
    }

    private void setExecutorPoolSize(ClientConfig clientConfig, HazelcastClientConfig config) {
        config.executorPoolSize
          .ifPresent(clientConfig::setExecutorPoolSize);
    }

}
