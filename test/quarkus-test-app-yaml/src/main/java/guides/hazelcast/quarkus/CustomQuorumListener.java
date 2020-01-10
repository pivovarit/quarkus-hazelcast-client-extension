package guides.hazelcast.quarkus;

import com.hazelcast.quorum.QuorumEvent;
import com.hazelcast.quorum.QuorumListener;

class CustomQuorumListener implements QuorumListener {
    @Override
    public void onChange(QuorumEvent quorumEvent) {
        System.out.println("quorum listener called");
    }
}
