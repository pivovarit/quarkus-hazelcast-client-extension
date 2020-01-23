package io.quarkus.hazelcast.client;

import com.hazelcast.client.config.ClientClasspathXmlConfig;
import com.hazelcast.client.config.ClientClasspathYamlConfig;
import com.hazelcast.client.config.ClientConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Grzegorz Piwowarek
 */
class HazelcastConfigurationResolver {
    private static final String CONFIG_FILENAME = "hazelcast-client";

    private final HazelcastConfigurationParser parser = new HazelcastConfigurationParser();

    ClientConfig resolveClientConfig(HazelcastClientConfig config) {
        return resolveFromConfigFile().orElseGet(() -> parser.fromApplicationProperties(config));
    }

    private Optional<ClientConfig> resolveFromConfigFile() {
        boolean yml = exists(withExtension("yml"));
        boolean yaml = exists(withExtension("yaml"));
        boolean xml = exists(withExtension("xml"));

        if (Stream.of(yml, yaml, xml).mapToInt(b -> b ? 1 : 0).sum() > 1) {
            throw new RuntimeException("max one configuration file is supported");
        }

        if (yml) {
            return Optional.of(new ClientClasspathYamlConfig(withExtension("yml")));
        } else if (yaml) {
            return Optional.of(new ClientClasspathYamlConfig(withExtension("yaml")));
        } else if (xml) {
            return Optional.of(new ClientClasspathXmlConfig(withExtension("xml")));
        }
        return Optional.empty();
    }

    private boolean exists(String configFileName) {
        // Files.exist and referencing resources by path doesn't work on native
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
