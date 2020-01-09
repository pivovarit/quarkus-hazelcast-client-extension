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
    private static final String CONFIG_FILENAME = "hazelcast-client";

    public void configureRuntimeProperties(HazelcastClientConfig config) {
        HazelcastClientProducer hazelcastClientProducer = Arc.container().instance(HazelcastClientProducer.class).get();
        hazelcastClientProducer.setHazelcastClientConfig(config);
        hazelcastClientProducer.setClientConfig(resolveClientConfig());
    }

    private boolean exists(String s) {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(s)) {
            return stream != null;
        } catch (IOException e) {
            return false;
        }
    }

    private ClientConfig resolveClientConfig() {
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
        return null;
    }
}
