package io.quarkus.hazelcast.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientClasspathXmlConfig;
import com.hazelcast.client.config.ClientClasspathYamlConfig;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.Client;
import com.hazelcast.core.HazelcastInstance;
import io.quarkus.arc.DefaultBean;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@ApplicationScoped
public class HazelcastClientProducer {
    private final AtomicReference<HazelcastInstance> instance = new AtomicReference<>(null);

    private HazelcastClientConfig hazelcastClientConfig;

    private Boolean ymlConfigPresent = null;
    private Boolean yamlConfigPresent = null;
    private Boolean xmlConfigPresent = null;

    @Produces
    @Singleton
    @DefaultBean
    public ClientConfig hazelcastConfigClientInstance() throws URISyntaxException {
        if (ymlConfigPresent) {
            return new ClientClasspathYamlConfig("hazelcast-client.yml");
        } else if (yamlConfigPresent) {
            return new ClientClasspathYamlConfig("hazelcast-client.yaml");
        } else if (xmlConfigPresent) {
            return new ClientClasspathXmlConfig("hazelcast-client.xml");
        }

        return fromApplicationProperties();
    }

    private boolean exists(String s) throws URISyntaxException {
        InputStream stream = HazelcastClientProducer.class.getResourceAsStream(s);
        try (Scanner scanner = new Scanner(stream, "UTF-8")) {
            return !scanner.useDelimiter("\\A").next().isEmpty();
        }
    }


    @Produces
    @Singleton
    @DefaultBean
    public HazelcastInstance hazelcastClientInstance(ClientConfig config) {
        HazelcastInstance instance = HazelcastClient.newHazelcastClient(config);
        this.instance.set(instance);
        return instance;
    }

    private ClientConfig fromApplicationProperties() {
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

    public void setYmlConfigPresent(boolean ymlConfigPresent) {
        this.ymlConfigPresent = ymlConfigPresent;
    }

    public void setYamlConfigPresent(boolean yamlConfigPresent) {
        this.yamlConfigPresent = yamlConfigPresent;
    }

    public void setXmlConfigPresent(boolean xmlConfigPresent) {
        this.xmlConfigPresent = xmlConfigPresent;
    }
}
