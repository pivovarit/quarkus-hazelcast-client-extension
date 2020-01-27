package io.quarkus.hazelcast.client;

import com.hazelcast.client.config.ClientConfig;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author Grzegorz Piwowarek
 */
class HazelcastConfigurationParser {

    ClientConfig fromApplicationProperties(HazelcastClientConfig config, ClientConfig clientConfig) {
        setClusterAddress(clientConfig, config);
        setGroupName(clientConfig, config);
        setLabels(clientConfig, config);

        setOutboundPorts(clientConfig, config);
        setOutboundPortDefinitions(clientConfig, config);

        setConnectionTimeout(clientConfig, config);
        setExecutorPoolSize(clientConfig, config);

        return clientConfig;
    }

    private void setClusterAddress(ClientConfig clientConfig, HazelcastClientConfig config) {
        config.clusterMembers
          .ifPresent(members -> clientConfig.getNetworkConfig().addAddress(members.split(",")));
    }

    private void setGroupName(ClientConfig clientConfig, HazelcastClientConfig config) {
        config.clusterName
          .filter(s -> !s.isEmpty())
          .ifPresent(clientConfig::setClusterName);
    }

    private void setLabels(ClientConfig clientConfig, HazelcastClientConfig config) {
        config.labels
          .map(s -> Arrays.stream(s.split(","))).orElseGet(Stream::empty)
          .forEach(clientConfig::addLabel);
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
