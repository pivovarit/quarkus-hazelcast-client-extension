package guides.hazelcast.quarkus;

import com.hazelcast.core.MigrationEvent;
import com.hazelcast.core.MigrationListener;

class CustomMigrationListener implements MigrationListener {
    @Override
    public void migrationStarted(MigrationEvent migrationEvent) {
        System.out.println("migration started");
    }

    @Override
    public void migrationCompleted(MigrationEvent migrationEvent) {
        System.out.println("migration completed");
    }

    @Override
    public void migrationFailed(MigrationEvent migrationEvent) {
        System.out.println("migration failed");
    }
}
