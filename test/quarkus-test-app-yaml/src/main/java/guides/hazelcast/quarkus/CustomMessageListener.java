package guides.hazelcast.quarkus;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

class CustomMessageListener implements MessageListener<Object> {
    @Override
    public void onMessage(Message<Object> message) {
        System.out.println("message listener called");
    }
}
