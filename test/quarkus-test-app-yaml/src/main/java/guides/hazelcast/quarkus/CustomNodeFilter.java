package guides.hazelcast.quarkus;

import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.NodeFilter;

class CustomNodeFilter implements NodeFilter {
    @Override
    public boolean test(DiscoveryNode candidate) {
        return true;
    }
}
