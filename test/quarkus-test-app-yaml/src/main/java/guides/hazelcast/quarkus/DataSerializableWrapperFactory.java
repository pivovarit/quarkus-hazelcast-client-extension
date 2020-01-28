package guides.hazelcast.quarkus;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;


class DataSerializableWrapperFactory implements DataSerializableFactory {
    @Override
    public IdentifiedDataSerializable create(int typeId) {
        if (typeId == 42) {
            return new DataSerializableWrapper();
        }

        throw new IllegalArgumentException(typeId + " unknown");
    }
}
