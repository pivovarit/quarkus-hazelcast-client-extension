package io.quarkus.hazelcast.client;

import com.hazelcast.client.HazelcastClient;
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
    private ClientConfig clientConfig;

    @Produces
    @Singleton
    @DefaultBean
    public HazelcastInstance hazelcastClientInstance() {
        return newHazelcastClient(requireNonNull(clientConfig, "clientConfig not initialized properly"));
    }

    @PreDestroy
    public void destroy() {
        HazelcastClient.shutdownAll();
    }

    public void injectClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }
}
