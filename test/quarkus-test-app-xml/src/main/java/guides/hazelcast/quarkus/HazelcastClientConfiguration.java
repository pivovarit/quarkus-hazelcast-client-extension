package guides.hazelcast.quarkus;

import com.hazelcast.client.config.ClientClasspathXmlConfig;
import com.hazelcast.client.config.ClientConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class HazelcastClientConfiguration {

    @Produces
    ClientConfig createInstance() {
        return new ClientClasspathXmlConfig("hazelcast.xml");
    }
}
