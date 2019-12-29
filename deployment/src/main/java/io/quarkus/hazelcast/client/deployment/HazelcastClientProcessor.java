package io.quarkus.hazelcast.client.deployment;

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
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBundleBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveHierarchyBuildItem;
import io.quarkus.hazelcast.client.HazelcastClientConfig;
import io.quarkus.hazelcast.client.HazelcastClientProducer;
import io.quarkus.hazelcast.client.HazelcastRecorder;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;

class HazelcastClientProcessor {

    private static final String FEATURE = "hazelcast-client";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerXmlParsingClasses(BuildProducer<ReflectiveClassBuildItem> reflectiveClass, BuildProducer<NativeImageResourceBundleBuildItem> bundle) {
        reflectiveClass.produce(new ReflectiveClassBuildItem(false, false,
          "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",
          "com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl",
          "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
          "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl",
          "com.sun.xml.bind.v2.ContextFactory",
          "com.sun.xml.internal.bind.v2.ContextFactory",
          "com.sun.org.apache.xpath.internal.functions.FuncNot",
          "com.sun.xml.internal.stream.XMLInputFactoryImpl"));
        bundle.produce(new NativeImageResourceBundleBuildItem("com.sun.org.apache.xml.internal.serializer.utils.SerializerMessages"));
    }

    @BuildStep
    void registerConfigClasses(BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, false, EventJournalConfig.class));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, false, MerkleTreeConfig.class));
    }

    @BuildStep
    void registerResources(BuildProducer<NativeImageResourceBuildItem> resources) {
        resources.produce(new NativeImageResourceBuildItem("hazelcast.yml", "hazelcast.xml"));
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
