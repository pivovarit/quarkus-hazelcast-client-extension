package guides.hazelcast.quarkus;

import com.hazelcast.nio.serialization.Serializer;

class CustomSerializer implements Serializer {
    @Override
    public int getTypeId() {
        return 123;
    }

    @Override
    public void destroy() {

    }
}
