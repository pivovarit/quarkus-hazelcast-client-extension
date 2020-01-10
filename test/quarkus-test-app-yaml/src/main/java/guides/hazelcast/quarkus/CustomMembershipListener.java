package guides.hazelcast.quarkus;

import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

class CustomMembershipListener implements MembershipListener {
    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        System.out.println("member added " + membershipEvent.toString());
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        System.out.println("member removed " + membershipEvent.toString());

    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        System.out.println("member attr change " + memberAttributeEvent.toString());

    }
}
