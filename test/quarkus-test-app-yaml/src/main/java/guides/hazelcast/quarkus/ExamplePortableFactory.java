package guides.hazelcast.quarkus;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

class ExamplePortableFactory implements PortableFactory {
    @Override
    public Portable create(int classId) {
        return null;
    }
}
