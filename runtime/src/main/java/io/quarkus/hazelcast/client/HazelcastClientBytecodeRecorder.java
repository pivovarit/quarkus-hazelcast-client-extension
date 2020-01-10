package io.quarkus.hazelcast.client;

import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.runtime.annotations.Recorder;

/**
 * @author Grzegorz Piwowarek
 */
@Recorder
public class HazelcastClientBytecodeRecorder {

    public BeanContainerListener configureBuildTimeProperties(HazelcastClientBuildTimeConfig config) {
        return container -> {
            container.instance(HazelcastClientProducer.class)
              .injectClientConfig(new HazelcastConfigurationResolver().resolveClientConfig(config));
        };
    }
}
