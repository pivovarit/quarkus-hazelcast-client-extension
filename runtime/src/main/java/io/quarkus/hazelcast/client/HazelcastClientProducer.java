package io.quarkus.hazelcast.client;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import io.quarkus.arc.DefaultBean;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import static com.hazelcast.client.HazelcastClient.newHazelcastClient;
import static java.util.Objects.requireNonNull;

/**
 * @author Grzegorz Piwowarek
 */
@ApplicationScoped
public class HazelcastClientProducer {
    private volatile HazelcastInstance instance = null;

    HazelcastClientConfig config;

    @Produces
    @Singleton
    @DefaultBean
    public HazelcastInstance hazelcastClientInstance() {
        HazelcastInstance instance = newHazelcastClient(new HazelcastConfigurationResolver().resolveClientConfig(config));
        this.instance = instance;
        return instance;
    }

    @PreDestroy
    public void destroy() {
        HazelcastInstance hazelcastInstance = instance;
        if (hazelcastInstance != null) {
            hazelcastInstance.shutdown();
        }
    }

    public void injectConfig(HazelcastClientConfig config) {
        this.config = config;
    }
}
