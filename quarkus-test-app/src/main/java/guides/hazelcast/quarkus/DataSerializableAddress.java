package guides.hazelcast.quarkus;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

import java.io.IOException;
import java.util.Objects;

public class DataSerializableAddress implements com.hazelcast.nio.serialization.DataSerializable {
    private String street;
    private int zipCode;
    private String city;
    private String state;

    public DataSerializableAddress() {
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setZipCode(int zipCode) {
        this.zipCode = zipCode;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        street = in.readUTF();
        zipCode = in.readInt();
        city = in.readUTF();
        state = in.readUTF();
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(street);
        out.writeInt(zipCode);
        out.writeUTF(city);
        out.writeUTF(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataSerializableAddress address = (DataSerializableAddress) o;
        return zipCode == address.zipCode &&
          Objects.equals(street, address.street) &&
          Objects.equals(city, address.city) &&
          Objects.equals(state, address.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, zipCode, city, state);
    }

    @Override
    public String toString() {
        return String.format("Address{street='%s', zipCode=%d, city='%s', state='%s'}", street, zipCode, city, state);
    }
}
