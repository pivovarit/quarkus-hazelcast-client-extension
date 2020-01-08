package guides.hazelcast.quarkus;

import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.partitiongroup.PartitionGroupStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

class CustomDiscoveryStrategy implements DiscoveryStrategy {
    @Override
    public void start() {

    }

    @Override
    public Iterable<DiscoveryNode> discoverNodes() {
        return new ArrayList<>();
    }

    @Override
    public void destroy() {

    }

    @Override
    public PartitionGroupStrategy getPartitionGroupStrategy() {
        return ArrayList::new;
    }

    @Override
    public Map<String, Object> discoverLocalMetadata() {
        return Collections.emptyMap();
    }
}
