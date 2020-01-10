package guides.hazelcast.quarkus;

class CustomItemListener implements com.hazelcast.core.ItemListener<String> {

    @Override
    public void itemAdded(com.hazelcast.core.ItemEvent<String> item) {
        System.out.println("item added " + item.toString());
    }

    @Override
    public void itemRemoved(com.hazelcast.core.ItemEvent<String> item) {
        System.out.println("item removed " + item.toString());
    }
}
