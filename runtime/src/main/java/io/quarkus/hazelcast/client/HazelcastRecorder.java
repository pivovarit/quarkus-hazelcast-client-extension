package io.quarkus.hazelcast.client;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class HazelcastRecorder {
    public void configureRuntimeProperties(HazelcastClientConfig config) {
        Arc.container()
          .instance(HazelcastClientProducer.class).get()
          .injectClientConfig(new HazelcastConfigurationResolver().resolveClientConfig(config));
    }
}
