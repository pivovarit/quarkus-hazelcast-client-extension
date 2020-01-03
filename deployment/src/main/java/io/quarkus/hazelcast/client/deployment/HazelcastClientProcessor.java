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
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import java.util.stream.IntStream;

class HazelcastClientProcessor {

    private static final String FEATURE = "hazelcast-client";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerXmlParsingClasses(
      BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
      BuildProducer<NativeImageResourceBundleBuildItem> bundles,
      BuildProducer<NativeImageResourceBuildItem> resources) {
        reflectiveClass.produce(new ReflectiveClassBuildItem(false, false,
          "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",
          "com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl",
          "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
          "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl",
          "com.sun.xml.bind.v2.ContextFactory",
          "com.sun.xml.internal.bind.v2.ContextFactory",
          "com.sun.org.apache.xpath.internal.functions.FuncNot",
          "com.sun.org.apache.xerces.internal.impl.dv.xs.SchemaDVFactoryImpl",
          "com.sun.xml.internal.stream.XMLInputFactoryImpl"));

        bundles.produce(new NativeImageResourceBundleBuildItem("com.sun.org.apache.xml.internal.serializer.utils.SerializerMessages"));
        bundles.produce(new NativeImageResourceBundleBuildItem("com.sun.org.apache.xerces.internal.impl.msg.XMLMessages"));
        bundles.produce(new NativeImageResourceBundleBuildItem("com.sun.org.apache.xerces.internal.impl.msg.XMLSchemaMessages"));
        bundles.produce(new NativeImageResourceBundleBuildItem("com.sun.org.apache.xerces.internal.impl.xpath.regex.message"));

        resources.produce(new NativeImageResourceBuildItem("com/sun/org/apache/xml/internal/serializer/output_xml.properties"));

        IntStream.rangeClosed(1, 12).boxed().map(i -> String.format("hazelcast-client-config-3.%d.xsd", i))
          .forEach(resource -> resources.produce(new NativeImageResourceBuildItem(resource)));
        resources.produce(new NativeImageResourceBuildItem("hazelcast-client-config-4.0.xsd"));
    }

    @BuildStep
    void registerConfigClasses(BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, false, EventJournalConfig.class, MerkleTreeConfig.class));
    }

    @BuildStep
    void registerConfigurationResources(BuildProducer<NativeImageResourceBuildItem> resources) {
        resources.produce(new NativeImageResourceBuildItem("hazelcast.yml", "hazelcast.xml"));
    }

    @BuildStep
    void registerCustomImplementationClasses(
      CombinedIndexBuildItem combinedIndexBuildItem,
      BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass) {

        registerAllImplementations(combinedIndexBuildItem, reflectiveHierarchyClass,
          DataSerializable.class.getName(),
          "com.hazelcast.nio.SocketInterceptor",
          "com.hazelcast.nio.ssl.SSLContextFactory",
          "com.hazelcast.spi.discovery.DiscoveryStrategy",
          "com.hazelcast.core.MembershipListener",
          "com.hazelcast.core.MigrationListener",
          "com.hazelcast.security.ICredentialsFactory",
          "com.hazelcast.core.EntryListener",
          "com.hazelcast.map.listener.MapListener",
          "com.hazelcast.client.ClientExtension");
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

    private static void registerAllImplementations(CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass, String... classNames) {
        for (String className : classNames) {
            combinedIndexBuildItem.getIndex().getAllKnownImplementors(DotName.createSimple(className)).stream()
              .map(ci -> new ReflectiveHierarchyBuildItem(Type.create(ci.name(), Type.Kind.CLASS)))
              .forEach(reflectiveHierarchyClass::produce);
        }
    }
}
