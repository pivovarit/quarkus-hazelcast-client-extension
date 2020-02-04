package io.quarkus.hazelcast.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.quarkus.arc.DefaultBean;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import static com.hazelcast.client.HazelcastClient.newHazelcastClient;

/**
 * @author Grzegorz Piwowarek
 */
@ApplicationScoped
public class HazelcastClientProducer {

    HazelcastClientConfig config;

    @Produces
    @Singleton
    @DefaultBean
    public HazelcastInstance hazelcastClientInstance() {
        return newHazelcastClient(new HazelcastConfigurationResolver()
          .resolveClientConfig(config));
    }

    @PreDestroy
    public void destroy() {
        HazelcastClient.shutdownAll();
    }

    public void injectConfig(HazelcastClientConfig config) {
        this.config = config;
    }
}
