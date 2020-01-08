package io.quarkus.hazelcast.client;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;

import java.io.IOException;
import java.io.InputStream;

@Recorder
public class HazelcastRecorder {
    public void configureRuntimeProperties(HazelcastClientConfig config) {
        HazelcastClientProducer hazelcastClientProducer = Arc.container().instance(HazelcastClientProducer.class).get();
        hazelcastClientProducer.setHazelcastClientConfig(config);
        hazelcastClientProducer.setYmlConfigPresent(exists("hazelcast-client.yml"));
        hazelcastClientProducer.setYamlConfigPresent(exists("hazelcast-client.yaml"));
        hazelcastClientProducer.setXmlConfigPresent(exists("hazelcast-client.xml"));
    }

    private boolean exists(String s) {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(s)) {
            return stream != null;
        } catch (IOException e) {
            return false;
        }
    }
}
