package io.quarkus.hazelcast.client;

import com.hazelcast.client.config.ClientConfig;

import java.util.Arrays;
import java.util.stream.Stream;

class HazelcastConfigurationParser {

    ClientConfig fromApplicationProperties(HazelcastClientBuildTimeConfig config) {
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

    private void setClusterAddress(ClientConfig clientConfig, HazelcastClientBuildTimeConfig config) {
        clientConfig.getNetworkConfig().addAddress(config.clusterMembers.split(","));
    }

    private void setGroupName(ClientConfig clientConfig, HazelcastClientBuildTimeConfig config) {
        config.groupName
          .filter(s -> !s.isEmpty())
          .ifPresent(groupName -> clientConfig.getGroupConfig().setName(groupName));
    }

    private void setLabels(ClientConfig clientConfig, HazelcastClientBuildTimeConfig config) {
        config.labels
          .map(s -> Arrays.stream(s.split(","))).orElseGet(Stream::empty)
          .forEach(clientConfig::addLabel);
    }

    private void setConnectionAttemptPeriod(ClientConfig clientConfig, HazelcastClientBuildTimeConfig config) {
        config.connectionAttemptPeriod
          .ifPresent(period -> clientConfig.getNetworkConfig().setConnectionAttemptPeriod(period));
    }

    private void setConnectionAttemptLimit(ClientConfig clientConfig, HazelcastClientBuildTimeConfig config) {
        config.connectionAttemptLimit
          .ifPresent(attempts -> clientConfig.getNetworkConfig().setConnectionAttemptLimit(attempts));
    }

    private void setConnectionTimeout(ClientConfig clientConfig, HazelcastClientBuildTimeConfig config) {
        config.connectionTimeout
          .ifPresent(timeout -> clientConfig.getNetworkConfig().setConnectionTimeout(timeout));
    }

    private void setOutboundPortDefinitions(ClientConfig clientConfig, HazelcastClientBuildTimeConfig config) {
        config.outboundPortDefinitions
          .map(str -> Arrays.stream(str.split(","))).orElseGet(Stream::empty)
          .forEach(definition -> clientConfig.getNetworkConfig().addOutboundPortDefinition(definition));
    }

    private void setOutboundPorts(ClientConfig clientConfig, HazelcastClientBuildTimeConfig config) {
        config.outboundPorts
          .map(str -> Arrays.stream(str.split(","))).orElseGet(Stream::empty)
          .forEach(port -> clientConfig.getNetworkConfig().addOutboundPort(Integer.parseInt(port)));
    }

    private void setExecutorPoolSize(ClientConfig clientConfig, HazelcastClientBuildTimeConfig config) {
        config.executorPoolSize
          .ifPresent(clientConfig::setExecutorPoolSize);
    }
}
