package io.quarkus.hazelcast.client.deployment;

import com.hazelcast.client.cache.impl.HazelcastClientCachingProvider;
import com.hazelcast.config.EventJournalConfig;
import com.hazelcast.config.MerkleTreeConfig;
import com.hazelcast.nio.serialization.DataSerializable;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveHierarchyBuildItem;
import io.quarkus.hazelcast.client.HazelcastClientConfig;
import io.quarkus.hazelcast.client.HazelcastClientProducer;
import io.quarkus.hazelcast.client.HazelcastRecorder;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;

import javax.inject.Inject;

class HazelcastClientProcessor {

    private static final String FEATURE = "hazelcast-client";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerConfigClasses(BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, false, EventJournalConfig.class));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, false, MerkleTreeConfig.class));
    }

    @BuildStep
    void registerDataSerializableClasses(
      CombinedIndexBuildItem combinedIndexBuildItem,
      BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass) {
        IndexView index = combinedIndexBuildItem.getIndex();

        for (ClassInfo ci : index.getAllKnownImplementors(DotName.createSimple(DataSerializable.class.getName()))) {
            reflectiveHierarchyClass.produce(new ReflectiveHierarchyBuildItem(Type.create(ci.name(), Type.Kind.CLASS)));
        }
    }

    @BuildStep
    void setup(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(HazelcastClientProducer.class));
    }

    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    HazelcastClientConfiguredBuildItem configureRuntimeProperties(
      HazelcastRecorder recorder,
      HazelcastClientConfig config) {
        recorder.configureRuntimeProperties(config);
        return new HazelcastClientConfiguredBuildItem();
    }
}
