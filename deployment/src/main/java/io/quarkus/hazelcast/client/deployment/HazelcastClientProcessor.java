package io.quarkus.hazelcast.client.deployment;

import com.hazelcast.client.cache.impl.HazelcastClientCachingProvider;
import com.hazelcast.config.EventJournalConfig;
import com.hazelcast.config.MerkleTreeConfig;
import com.hazelcast.config.replacer.EncryptionReplacer;
import com.hazelcast.config.replacer.PropertyReplacer;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.nio.serialization.DataSerializableFactory;
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
    void registerDynamicallyCreatedClasses(BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
        reflectiveClass.produce(new ReflectiveClassBuildItem(false, false, HazelcastClientCachingProvider.class));
    }

    @BuildStep
    void registerXmlParsingClasses(
      BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
      BuildProducer<NativeImageResourceBundleBuildItem> bundles,
      BuildProducer<NativeImageResourceBuildItem> resources) {
        reflectiveClass.produce(new ReflectiveClassBuildItem(false, false,
          "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",
          "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl",
          "com.sun.xml.bind.v2.ContextFactory",
          "com.sun.xml.internal.stream.XMLInputFactoryImpl",
          "com.sun.org.apache.xpath.internal.functions.FuncNot",
          "com.sun.xml.internal.bind.v2.ContextFactory",
          "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
          "com.sun.org.apache.xerces.internal.impl.dv.xs.SchemaDVFactoryImpl",
          "com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl"));

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
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, false,
          EventJournalConfig.class,
          MerkleTreeConfig.class,
          EncryptionReplacer.class,
          PropertyReplacer.class));
    }

    @BuildStep
    void registerConfigurationResources(BuildProducer<NativeImageResourceBuildItem> resources) {
        resources.produce(new NativeImageResourceBuildItem("hazelcast-client.yml", "hazelcast-client.xml"));
    }

    @BuildStep
    void registerCustomImplementationClasses(
      CombinedIndexBuildItem combinedIndexBuildItem,
      BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass) {

        registerAllImplementations(combinedIndexBuildItem, reflectiveHierarchyClass,
          DataSerializable.class,
          DataSerializableFactory.class,
          com.hazelcast.nio.SocketInterceptor.class,
          com.hazelcast.nio.ssl.SSLContextFactory.class,
          com.hazelcast.nio.serialization.Serializer.class,
          com.hazelcast.spi.discovery.DiscoveryStrategy.class,
          com.hazelcast.security.ICredentialsFactory.class,
          com.hazelcast.core.MembershipListener.class,
          com.hazelcast.core.MigrationListener.class,
          com.hazelcast.core.EntryListener.class,
          com.hazelcast.core.MessageListener.class,
          com.hazelcast.core.ItemListener.class,
          com.hazelcast.map.listener.MapListener.class,
          com.hazelcast.quorum.QuorumListener.class,
          com.hazelcast.quorum.QuorumFunction.class,
          com.hazelcast.config.replacer.spi.ConfigReplacer.class,
          com.hazelcast.client.ClientExtension.class,
          com.hazelcast.client.spi.ClientProxyFactory.class);

        registerAllSubclasses(combinedIndexBuildItem, reflectiveHierarchyClass,com.hazelcast.client.connection.ClientConnectionStrategy.class);
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

    private static void registerAllImplementations(CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass, Class<?>... classNames) {
        for (Class<?> klass : classNames) {
            combinedIndexBuildItem.getIndex().getAllKnownImplementors(DotName.createSimple(klass.getName())).stream()
              .map(ci -> new ReflectiveHierarchyBuildItem(Type.create(ci.name(), Type.Kind.CLASS)))
              .forEach(reflectiveHierarchyClass::produce);
        }
    }

    private static void registerAllSubclasses(CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass, Class<?>... classNames) {
        for (Class<?> klass : classNames) {
            combinedIndexBuildItem.getIndex().getAllKnownSubclasses(DotName.createSimple(klass.getName())).stream()
              .map(ci -> new ReflectiveHierarchyBuildItem(Type.create(ci.name(), Type.Kind.CLASS)))
              .forEach(reflectiveHierarchyClass::produce);
        }
    }
}
