package io.quarkus.hazelcast.client;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import io.quarkus.arc.DefaultBean;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicReference;

import static com.hazelcast.client.HazelcastClient.newHazelcastClient;
import static java.util.Objects.requireNonNull;

/**
 * @author Grzegorz Piwowarek
 */
@ApplicationScoped
public class HazelcastClientProducer {
    private volatile HazelcastInstance instance = null;

    private ClientConfig clientConfig;

    @Produces
    @Singleton
    @DefaultBean
    public HazelcastInstance hazelcastClientInstance() {
        HazelcastInstance instance = newHazelcastClient(requireNonNull(clientConfig, "clientConfig not initialized properly"));
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

    public void injectClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }
}
