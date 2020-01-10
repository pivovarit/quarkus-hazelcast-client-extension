package io.quarkus.hazelcast.client;

import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class HazelcastRecorder {

    public BeanContainerListener configureBuildTimeProperties(HazelcastClientBuildTimeConfig config) {
        return container -> {
            container.instance(HazelcastClientProducer.class)
              .injectClientConfig(new HazelcastConfigurationResolver().resolveClientConfig(config));
        };
    }
}
