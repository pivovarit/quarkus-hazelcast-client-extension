package io.quarkus.hazelcast.client;

import com.hazelcast.client.config.ClientClasspathXmlConfig;
import com.hazelcast.client.config.ClientClasspathYamlConfig;
import com.hazelcast.client.config.ClientConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

class HazelcastConfigurationResolver {
    private static final String CONFIG_FILENAME = "hazelcast-client";

    private final HazelcastConfigurationParser parser = new HazelcastConfigurationParser();

    ClientConfig resolveBuildTimeClientConfig(HazelcastClientBuildTimeConfig config) {
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

        return parser.fromApplicationProperties(config);
    }

    private boolean exists(String configFileName) {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFileName)) {
            return stream != null;
        } catch (IOException e) {
            return false;
        }
    }
}
