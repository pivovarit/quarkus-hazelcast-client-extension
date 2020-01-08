package io.quarkus.hazelcast.client;

import com.hazelcast.client.config.ClientClasspathXmlConfig;
import com.hazelcast.client.config.ClientClasspathYamlConfig;
import com.hazelcast.client.config.ClientConfig;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

@Recorder
public class HazelcastRecorder {
    public void configureRuntimeProperties(HazelcastClientConfig config) {
        HazelcastClientProducer hazelcastClientProducer = Arc.container().instance(HazelcastClientProducer.class).get();
        hazelcastClientProducer.setHazelcastClientConfig(config);
        hazelcastClientProducer.setClientConfig(getClientConfig());
    }

    private boolean exists(String s) {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(s)) {
            return stream != null;
        } catch (IOException e) {
            return false;
        }
    }

    private ClientConfig getClientConfig() {
        boolean ymlConfig = exists("hazelcast-client.yml");
        boolean yamlConfig = exists("hazelcast-client.yaml");
        boolean xmlConfig = exists("hazelcast-client.xml");

        if (Stream.of(ymlConfig, yamlConfig, xmlConfig).mapToInt(b -> b ? 1 : 0).sum() > 1) {
            throw new RuntimeException("max one configuration file is supported");
        }

        if (ymlConfig) {
            return new ClientClasspathYamlConfig("hazelcast-client.yml");
        } else if (yamlConfig) {
            return new ClientClasspathYamlConfig("hazelcast-client.yaml");
        } else if (xmlConfig) {
            return new ClientClasspathXmlConfig("hazelcast-client.xml");
        }
        return null;
    }
}
