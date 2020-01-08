package io.quarkus.hazelcast.client;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;

import java.nio.file.Files;
import java.nio.file.Paths;

@Recorder
public class HazelcastRecorder {
    public void configureRuntimeProperties(HazelcastClientConfig config) {
        HazelcastClientProducer hazelcastClientProducer = Arc.container().instance(HazelcastClientProducer.class).get();
        hazelcastClientProducer.setHazelcastClientConfig(config);
        hazelcastClientProducer.setYmlConfigPresent(exists("hazelcast-client.yml"));
        hazelcastClientProducer.setYamlConfigPresent(exists("hazelcast-client.yaml"));
        hazelcastClientProducer.setXmlConfigPresent(exists("hazelcast-client.xml"));
    }

    private static boolean exists(String path) {
        return Files.exists(Paths.get(path));
    }
}
