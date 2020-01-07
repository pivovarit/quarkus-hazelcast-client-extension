package guides.hazelcast.quarkus;

import com.hazelcast.core.MigrationEvent;
import com.hazelcast.core.MigrationListener;

class CustomMigrationListener implements MigrationListener {
    @Override
    public void migrationStarted(MigrationEvent migrationEvent) {
    }

    @Override
    public void migrationCompleted(MigrationEvent migrationEvent) {
    }

    @Override
    public void migrationFailed(MigrationEvent migrationEvent) {
    }
}
