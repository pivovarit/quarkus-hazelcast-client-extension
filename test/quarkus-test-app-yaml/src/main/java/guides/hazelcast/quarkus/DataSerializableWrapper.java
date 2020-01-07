package guides.hazelcast.quarkus;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

import java.io.IOException;

public class DataSerializableWrapper implements IdentifiedDataSerializable {
    private String value;

    public DataSerializableWrapper() {
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        value = in.readUTF();
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(value);
    }

    @Override
    public int getFactoryId() {
        return 42;
    }

    @Override
    public int getId() {
        return 42;
    }
}
