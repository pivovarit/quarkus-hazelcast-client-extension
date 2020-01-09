package io.quarkus.hazelcast.client;

import com.hazelcast.client.config.ClientClasspathXmlConfig;
import com.hazelcast.client.config.ClientClasspathYamlConfig;
import com.hazelcast.client.config.ClientConfig;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static io.quarkus.hazelcast.client.HazelcastClientProducer.CONFIG_FILENAME;

@Recorder
public class HazelcastRecorder {
    public void configureRuntimeProperties(HazelcastClientConfig config) {
        HazelcastClientProducer hazelcastClientProducer = Arc.container().instance(HazelcastClientProducer.class).get();
        hazelcastClientProducer.setHazelcastClientConfig(config);
        hazelcastClientProducer.setClientConfig(resolveClientConfig());
    }

    private boolean exists(String fileName) {
        return Files.exists(Paths.get(fileName));
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
