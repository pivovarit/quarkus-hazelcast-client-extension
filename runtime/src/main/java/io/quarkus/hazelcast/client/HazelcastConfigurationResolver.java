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

    ClientConfig resolveClientConfig(HazelcastClientConfig config) {
        boolean yml = exists(withExtension("yml"));
        boolean yaml = exists(withExtension("yaml"));
        boolean xml = exists(withExtension("xml"));

        if (Stream.of(yml, yaml, xml).mapToInt(b -> b ? 1 : 0).sum() > 1) {
            throw new RuntimeException("max one configuration file is supported");
        }

        if (yml) {
            return new ClientClasspathYamlConfig(withExtension("yml"));
        } else if (yaml) {
            return new ClientClasspathYamlConfig(withExtension("yaml"));
        } else if (xml) {
            return new ClientClasspathXmlConfig(withExtension("xml"));
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

    private static String withExtension(String extension) {
        return String.format("%s.%s", CONFIG_FILENAME, extension);
    }
}
