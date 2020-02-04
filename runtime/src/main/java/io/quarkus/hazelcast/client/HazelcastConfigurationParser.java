package io.quarkus.hazelcast.client;

import com.hazelcast.client.config.ClientConfig;

import java.net.Inet4Address;

class HazelcastConfigurationParser {

    ClientConfig fromApplicationProperties(HazelcastClientConfig config, ClientConfig clientConfig) {
        setClusterAddress(clientConfig, config);
        setLabels(clientConfig, config);

        setOutboundPorts(clientConfig, config);
        setOutboundPortDefinitions(clientConfig, config);

        setConnectionTimeout(clientConfig, config);

        return clientConfig;
    }

    private void setClusterAddress(ClientConfig clientConfig, HazelcastClientConfig config) {
        for (Inet4Address clusterMember : config.clusterMembers) {
            clientConfig.getNetworkConfig().addAddress(clusterMember.toString());
        }
    }

    private void setLabels(ClientConfig clientConfig, HazelcastClientConfig config) {
        for (String label : config.labels) {
            clientConfig.addLabel(label);
        }
    }

    private void setConnectionTimeout(ClientConfig clientConfig, HazelcastClientConfig config) {
        if (config.connectionTimeout.isPresent()) {
            int timeout = config.connectionTimeout.getAsInt();
            clientConfig.getNetworkConfig().setConnectionTimeout(timeout);
        }
    }

    private void setOutboundPortDefinitions(ClientConfig clientConfig, HazelcastClientConfig config) {
        for (String outboundPortDefinition : config.outboundPortDefinitions) {
            clientConfig.getNetworkConfig().addOutboundPortDefinition(outboundPortDefinition);
        }
    }

    private void setOutboundPorts(ClientConfig clientConfig, HazelcastClientConfig config) {
        for (Integer outboundPort : config.outboundPorts) {
            clientConfig.getNetworkConfig().addOutboundPort(outboundPort);
        }
    }
}
