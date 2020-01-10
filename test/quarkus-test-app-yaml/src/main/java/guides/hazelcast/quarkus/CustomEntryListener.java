package guides.hazelcast.quarkus;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

class CustomEntryListener implements EntryListener<String, DataSerializableWrapper> {
    @Override
    public void entryAdded(EntryEvent<String, DataSerializableWrapper> event) {
        System.out.println("entry added");
    }

    @Override
    public void entryEvicted(EntryEvent<String, DataSerializableWrapper> event) {
        System.out.println("entry evicted");
    }

    @Override
    public void entryRemoved(EntryEvent<String, DataSerializableWrapper> event) {
        System.out.println("entry removed");
    }

    @Override
    public void entryUpdated(EntryEvent<String, DataSerializableWrapper> event) {
        System.out.println("entry updated");
    }

    @Override
    public void mapCleared(MapEvent event) {
        System.out.println("map cleared");
    }

    @Override
    public void mapEvicted(MapEvent event) {
        System.out.println("map evicted");
    }
}
