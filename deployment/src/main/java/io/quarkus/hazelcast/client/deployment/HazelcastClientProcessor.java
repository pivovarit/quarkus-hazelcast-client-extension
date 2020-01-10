package io.quarkus.hazelcast.client.deployment;

import com.hazelcast.aws.AwsDiscoveryStrategy;
import com.hazelcast.client.cache.impl.HazelcastClientCachingProvider;
import com.hazelcast.client.connection.nio.DefaultCredentialsFactory;
import com.hazelcast.config.EventJournalConfig;
import com.hazelcast.config.MerkleTreeConfig;
import com.hazelcast.config.replacer.EncryptionReplacer;
import com.hazelcast.config.replacer.PropertyReplacer;
import com.hazelcast.config.replacer.spi.ConfigReplacer;
import com.hazelcast.gcp.GcpDiscoveryStrategy;
import com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategyFactory;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.PortableFactory;
import com.hazelcast.nio.serialization.Serializer;
import com.hazelcast.nio.ssl.BasicSSLContextFactory;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.NodeFilter;
import com.hazelcast.spi.discovery.multicast.MulticastDiscoveryStrategy;
import com.hazelcast.util.ICMPHelper;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerListenerBuildItem;
import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.JniBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBundleBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveHierarchyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeReinitializedClassBuildItem;
import io.quarkus.hazelcast.client.HazelcastClientBuildTimeConfig;
import io.quarkus.hazelcast.client.HazelcastClientProducer;
import io.quarkus.hazelcast.client.HazelcastRecorder;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import java.util.stream.IntStream;

class HazelcastClientProcessor {

    private static final String FEATURE = "hazelcast-client";

    HazelcastClientBuildTimeConfig buildTimeConfig;

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void configureNativeImageGeneration(
      BuildProducer<JniBuildItem> jni,
      BuildProducer<RuntimeReinitializedClassBuildItem> reinitializedClasses,
      BuildProducer<NativeImageResourceBuildItem> resources,
      BuildProducer<NativeImageResourceBundleBuildItem> bundles,
      BuildProducer<ReflectiveClassBuildItem> reflectiveClasses,
      BuildProducer<ReflectiveHierarchyBuildItem> reflectiveClassHierarchies,
      CombinedIndexBuildItem buildIndex) {

        registerConfigurationFiles(resources);
        registerXMLParsingUtilities(reflectiveClasses, bundles, resources);
        registerReflectivelyCreatedClasses(reflectiveClasses);
        registerICMPHelper(jni, reinitializedClasses, resources);
        registerUserImplementationsOfSerializableUtilities(buildIndex, reflectiveClassHierarchies);
        registerSSLUtilities(buildIndex, reflectiveClasses, reflectiveClassHierarchies);
        registerCustomCredentialFactories(buildIndex, reflectiveClasses, reflectiveClassHierarchies);
        registerCustomDiscoveryStrategiesClasses(buildIndex, reflectiveClasses, reflectiveClassHierarchies);
        registerCustomConfigReplacerClasses(buildIndex, reflectiveClasses, reflectiveClassHierarchies);
        registerCustomImplementationClasses(buildIndex, reflectiveClassHierarchies);
    }

    @BuildStep
    void setup(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(HazelcastClientProducer.class));
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    HazelcastClientConfiguredBuildItem resolveClientProperties(
      BuildProducer<BeanContainerListenerBuildItem> containerListenerProducer,
      HazelcastRecorder recorder) {

        BeanContainerListener configResolver = recorder.configureBuildTimeProperties(buildTimeConfig);
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(configResolver));

        return new HazelcastClientConfiguredBuildItem();
    }

    private void registerConfigurationFiles(BuildProducer<NativeImageResourceBuildItem> resources) {
        resources.produce(new NativeImageResourceBuildItem(
          "hazelcast-client.yml",
          "hazelcast-client.yaml",
          "hazelcast-client.xml"));
    }

    private void registerReflectivelyCreatedClasses(BuildProducer<ReflectiveClassBuildItem> reflectiveClasses) {
        reflectiveClasses.produce(new ReflectiveClassBuildItem(false, false,
          HazelcastClientCachingProvider.class,
          EventJournalConfig.class,
          MerkleTreeConfig.class));
    }

    private void registerCustomImplementationClasses(
      CombinedIndexBuildItem combinedIndexBuildItem,
      BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass) {

        registerAllImplementations(combinedIndexBuildItem, reflectiveHierarchyClass,
          com.hazelcast.nio.SocketInterceptor.class,
          com.hazelcast.core.MembershipListener.class,
          com.hazelcast.core.EntryListener.class,
          com.hazelcast.core.MessageListener.class,
          com.hazelcast.core.ItemListener.class,
          com.hazelcast.map.listener.MapListener.class,
          com.hazelcast.client.ClientExtension.class,
          com.hazelcast.client.spi.ClientProxyFactory.class);

        registerAllSubclasses(
          combinedIndexBuildItem,
          reflectiveHierarchyClass,
          com.hazelcast.client.connection.ClientConnectionStrategy.class);
    }

    private void registerCustomConfigReplacerClasses(
      CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
      BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass) {

        registerAllImplementations(combinedIndexBuildItem, reflectiveHierarchyClass, ConfigReplacer.class);
        reflectiveClass.produce(new ReflectiveClassBuildItem(false, false,
          EncryptionReplacer.class,
          PropertyReplacer.class));
    }

    private void registerCustomDiscoveryStrategiesClasses(
      CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
      BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass) {

        registerAllImplementations(combinedIndexBuildItem, reflectiveHierarchyClass,
          DiscoveryStrategy.class,
          NodeFilter.class);

        reflectiveClass.produce(new ReflectiveClassBuildItem(false, false,
          MulticastDiscoveryStrategy.class,
          AwsDiscoveryStrategy.class,
          GcpDiscoveryStrategy.class,
          HazelcastKubernetesDiscoveryStrategyFactory.class));
    }

    private void registerCustomCredentialFactories(
      CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
      BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass) {
        registerAllImplementations(
          combinedIndexBuildItem,
          reflectiveHierarchyClass,
          com.hazelcast.security.ICredentialsFactory.class);

        reflectiveClass.produce(
          new ReflectiveClassBuildItem(false, false, DefaultCredentialsFactory.class));
    }

    private void registerSSLUtilities(
      CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
      BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass) {

        registerAllImplementations(
          combinedIndexBuildItem,
          reflectiveHierarchyClass,
          com.hazelcast.nio.ssl.SSLContextFactory.class);
        reflectiveClass.produce(
          new ReflectiveClassBuildItem(false, false, BasicSSLContextFactory.class));
    }

    private void registerUserImplementationsOfSerializableUtilities(
      CombinedIndexBuildItem combinedIndexBuildItem,
      BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass) {
        registerAllImplementations(combinedIndexBuildItem, reflectiveHierarchyClass,
          DataSerializable.class,
          DataSerializableFactory.class,
          PortableFactory.class,
          Serializer.class);
    }

    private void registerICMPHelper(
      BuildProducer<JniBuildItem> jni,
      BuildProducer<RuntimeReinitializedClassBuildItem> reinitializedClasses,
      BuildProducer<NativeImageResourceBuildItem> resources) {
        resources.produce(new NativeImageResourceBuildItem(
          "lib/linux-x86/libicmp_helper.so",
          "lib/linux-x86_64/libicmp_helper.so"));
        reinitializedClasses.produce(new RuntimeReinitializedClassBuildItem(ICMPHelper.class.getName()));
        jni.produce(new JniBuildItem());
    }

    private void registerXMLParsingUtilities(
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

        bundles.produce(new NativeImageResourceBundleBuildItem(
          "com.sun.org.apache.xml.internal.serializer.utils.SerializerMessages"));
        bundles.produce(new NativeImageResourceBundleBuildItem(
          "com.sun.org.apache.xerces.internal.impl.msg.XMLMessages"));
        bundles.produce(new NativeImageResourceBundleBuildItem(
          "com.sun.org.apache.xerces.internal.impl.msg.XMLSchemaMessages"));
        bundles.produce(new NativeImageResourceBundleBuildItem(
          "com.sun.org.apache.xerces.internal.impl.xpath.regex.message"));

        resources.produce(new NativeImageResourceBuildItem(
          "com/sun/org/apache/xml/internal/serializer/output_xml.properties"));

        IntStream.rangeClosed(1, 12).boxed().map(i -> String.format("hazelcast-client-config-3.%d.xsd", i))
          .forEach(resource -> resources.produce(new NativeImageResourceBuildItem(resource)));
        resources.produce(new NativeImageResourceBuildItem("hazelcast-client-config-4.0.xsd"));
    }

    private static void registerAllImplementations(
      CombinedIndexBuildItem combinedIndexBuildItem,
      BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass,
      Class<?>... classNames) {
        for (Class<?> klass : classNames) {
            combinedIndexBuildItem.getIndex().getAllKnownImplementors(DotName.createSimple(klass.getName())).stream()
              .map(ci -> new ReflectiveHierarchyBuildItem(Type.create(ci.name(), Type.Kind.CLASS)))
              .forEach(reflectiveHierarchyClass::produce);
        }
    }

    private static void registerAllSubclasses(
      CombinedIndexBuildItem combinedIndexBuildItem,
      BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass,
      Class<?>... classNames) {
        for (Class<?> klass : classNames) {
            combinedIndexBuildItem.getIndex().getAllKnownSubclasses(DotName.createSimple(klass.getName())).stream()
              .map(ci -> new ReflectiveHierarchyBuildItem(Type.create(ci.name(), Type.Kind.CLASS)))
              .forEach(reflectiveHierarchyClass::produce);
        }
    }
}
