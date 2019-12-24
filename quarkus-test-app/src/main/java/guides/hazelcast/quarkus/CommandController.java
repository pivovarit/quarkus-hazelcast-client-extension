package guides.hazelcast.quarkus;

import com.hazelcast.core.HazelcastInstance;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.crypto.Data;
import java.util.concurrent.ConcurrentMap;

@Path("/hazelcast")
@RequestScoped
public class CommandController {

    @ConfigProperty(name = "CONTAINER_NAME")
    private String containerName;

    @Inject
    HazelcastInstance hazelcastInstance;

    private ConcurrentMap<String, Object> retrieveMap() {
        return hazelcastInstance.getMap("map");
    }

    @POST
    @Path("/put")
    @Produces(MediaType.APPLICATION_JSON)
    public CommandResponse put(@QueryParam("key") String key, @QueryParam("value") String value) {
        DataSerializableAddress dataSerializableAddress = new DataSerializableAddress();
        dataSerializableAddress.setCity("San Mateo");
        retrieveMap().put(key, dataSerializableAddress);
        return new CommandResponse(value, containerName);
    }

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public CommandResponse get(@QueryParam("key") String key) {
        Object value = retrieveMap().get(key);
        return new CommandResponse(value.toString(), containerName);
    }
}
