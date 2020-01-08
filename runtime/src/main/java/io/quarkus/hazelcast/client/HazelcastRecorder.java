package io.quarkus.hazelcast.client;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

@Recorder
public class HazelcastRecorder {
    public void configureRuntimeProperties(HazelcastClientConfig config) {
        HazelcastClientProducer hazelcastClientProducer = Arc.container().instance(HazelcastClientProducer.class).get();
        hazelcastClientProducer.setHazelcastClientConfig(config);
        hazelcastClientProducer.setYmlConfigPresent(existsNonempty("hazelcast-client.yml"));
        hazelcastClientProducer.setYamlConfigPresent(existsNonempty("hazelcast-client.yaml"));
        hazelcastClientProducer.setXmlConfigPresent(existsNonempty("hazelcast-client.xml"));
    }

    private boolean existsNonempty(String s) {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(s);
        if (stream == null) {
            return false;
        }
        try (Scanner scanner = new Scanner(stream, "UTF-8")) {
            try {
                return !scanner.useDelimiter("\\A").next().isEmpty();
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    // silent close
                }
            }
        }
    }
}
