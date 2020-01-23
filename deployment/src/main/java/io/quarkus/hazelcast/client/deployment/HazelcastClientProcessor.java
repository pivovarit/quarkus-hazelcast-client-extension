package io.quarkus.hazelcast.client.deployment;

import com.hazelcast.aws.AwsDiscoveryStrategy;
import com.hazelcast.aws.AwsDiscoveryStrategyFactory;
import com.hazelcast.client.ClientExtension;
import com.hazelcast.client.cache.impl.HazelcastClientCachingProvider;
import com.hazelcast.client.connection.nio.DefaultCredentialsFactory;
import com.hazelcast.com.fasterxml.jackson.core.JsonFactory;
import com.hazelcast.config.EventJournalConfig;
import com.hazelcast.config.MerkleTreeConfig;
import com.hazelcast.config.replacer.EncryptionReplacer;
import com.hazelcast.config.replacer.PropertyReplacer;
import com.hazelcast.config.replacer.spi.ConfigReplacer;
import com.hazelcast.core.MigrationListener;
import com.hazelcast.gcp.GcpDiscoveryStrategy;
import com.hazelcast.gcp.GcpDiscoveryStrategyFactory;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.PortableFactory;
import com.hazelcast.nio.serialization.Serializer;
import com.hazelcast.nio.ssl.BasicSSLContextFactory;
import com.hazelcast.quorum.QuorumListener;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;
import com.hazelcast.spi.discovery.NodeFilter;
import com.hazelcast.spi.discovery.multicast.MulticastDiscoveryStrategy;
import com.hazelcast.util.ICMPHelper;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.JniBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBundleBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveHierarchyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeReinitializedClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;
import io.quarkus.deployment.util.ServiceUtil;
import io.quarkus.hazelcast.client.HazelcastClientBytecodeRecorder;
import io.quarkus.hazelcast.client.HazelcastClientConfig;
import io.quarkus.hazelcast.client.HazelcastClientProducer;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * @author Grzegorz Piwowarek
 */
class HazelcastClientProcessor {

    private static final String FEATURE = "hazelcast-client";

    CombinedIndexBuildItem buildIndex;

    BuildProducer<ReflectiveClassBuildItem> reflectiveClasses;
    BuildProducer<NativeImageResourceBuildItem> resources;
    BuildProducer<ReflectiveHierarchyBuildItem> reflectiveClassHierarchies;
    BuildProducer<NativeImageResourceBundleBuildItem> bundles;
    BuildProducer<RuntimeReinitializedClassBuildItem> reinitializedClasses;
    BuildProducer<ServiceProviderBuildItem> services;

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void enableSSL(BuildProducer<ExtensionSslNativeSupportBuildItem> ssl) {
        ssl.produce(new ExtensionSslNativeSupportBuildItem(FEATURE));
    }

    @BuildStep
    void enableJNI(BuildProducer<JniBuildItem> jni) {
        jni.produce(new JniBuildItem());
    }

    @BuildStep
    void configureNativeImageGeneration() throws IOException {
        registerConfigurationFiles();
        registerXMLParsingUtilities();
        registerReflectivelyCreatedClasses();
        registerICMPHelper();
        registerUserImplementationsOfSerializableUtilities();
        registerSSLUtilities();
        registerCustomCredentialFactories();
        registerCustomDiscoveryStrategiesClasses();
        registerCustomConfigReplacerClasses();
        registerCustomImplementationClasses();
        registerServiceProviders(DiscoveryStrategyFactory.class);
        registerServiceProviders(ClientExtension.class);
        registerServiceProviders(JsonFactory.class);
    }

    @BuildStep
    void setup(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(HazelcastClientProducer.class));
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    HazelcastClientConfiguredBuildItem resolveClientProperties(HazelcastClientBytecodeRecorder recorder, HazelcastClientConfig config) {
        recorder.configureRuntimeProperties(config);
        return new HazelcastClientConfiguredBuildItem();
    }

    private void registerConfigurationFiles() {
        resources.produce(new NativeImageResourceBuildItem(
          "hazelcast-client.yml",
          "hazelcast-client.yaml",
          "hazelcast-client.xml"));
    }

    private void registerReflectivelyCreatedClasses() {
        reflectiveClasses.produce(new ReflectiveClassBuildItem(false, false,
          HazelcastClientCachingProvider.class,
          EventJournalConfig.class,
          MerkleTreeConfig.class));
    }

    private void registerCustomImplementationClasses() {

        registerTypeHierarchy(reflectiveClassHierarchies,
          com.hazelcast.nio.SocketInterceptor.class,
          com.hazelcast.core.MembershipListener.class,
          MigrationListener.class,
          QuorumListener.class,
          com.hazelcast.core.EntryListener.class,
          com.hazelcast.core.MessageListener.class,
          com.hazelcast.core.ItemListener.class,
          com.hazelcast.map.listener.MapListener.class,
          com.hazelcast.client.ClientExtension.class,
          com.hazelcast.client.spi.ClientProxyFactory.class);

        registerTypeHierarchy(
          reflectiveClassHierarchies,
          com.hazelcast.client.connection.ClientConnectionStrategy.class);
    }

    private void registerCustomConfigReplacerClasses() {

        registerTypeHierarchy(reflectiveClassHierarchies, ConfigReplacer.class);
        reflectiveClasses.produce(new ReflectiveClassBuildItem(false, false,
          EncryptionReplacer.class,
          PropertyReplacer.class));
    }

    private void registerCustomDiscoveryStrategiesClasses() {

        registerTypeHierarchy(reflectiveClassHierarchies,
          DiscoveryStrategy.class,
          NodeFilter.class);

        reflectiveClasses.produce(new ReflectiveClassBuildItem(false, false,
          MulticastDiscoveryStrategy.class,
          AwsDiscoveryStrategy.class,
          AwsDiscoveryStrategyFactory.class,
          GcpDiscoveryStrategy.class,
          GcpDiscoveryStrategyFactory.class));

        reflectiveClasses.produce(new ReflectiveClassBuildItem(false, false,
          "com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategyFactory",
          "com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategy"));
    }

    void registerServiceProviders(Class<?> klass) throws IOException {
        String service = "META-INF/services/" + klass.getName();

        Set<String> implementations = ServiceUtil.classNamesNamedIn(Thread.currentThread().getContextClassLoader(), service);

        services.produce(new ServiceProviderBuildItem(klass.getName(), new ArrayList<>(implementations)));
    }

    private void registerCustomCredentialFactories() {
        registerTypeHierarchy(
          reflectiveClassHierarchies,
          com.hazelcast.security.ICredentialsFactory.class);

        reflectiveClasses.produce(
          new ReflectiveClassBuildItem(false, false, DefaultCredentialsFactory.class));
    }

    private void registerSSLUtilities() {

        registerTypeHierarchy(
          reflectiveClassHierarchies,
          com.hazelcast.nio.ssl.SSLContextFactory.class);
        reflectiveClasses.produce(
          new ReflectiveClassBuildItem(false, false, BasicSSLContextFactory.class));
    }

    private void registerUserImplementationsOfSerializableUtilities() {
        registerTypeHierarchy(reflectiveClassHierarchies,
          DataSerializable.class,
          DataSerializableFactory.class,
          PortableFactory.class,
          Serializer.class);
    }

    private void registerICMPHelper() {
        resources.produce(new NativeImageResourceBuildItem(
          "lib/linux-x86/libicmp_helper.so",
          "lib/linux-x86_64/libicmp_helper.so"));
        reinitializedClasses.produce(new RuntimeReinitializedClassBuildItem(ICMPHelper.class.getName()));
    }

    private void registerXMLParsingUtilities() {
        reflectiveClasses.produce(new ReflectiveClassBuildItem(false, false,
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

    private static void registerTypeHierarchy(
      BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass,
      Class<?>... classNames) {

        for (Class<?> klass : classNames) {
            reflectiveHierarchyClass.produce(new ReflectiveHierarchyBuildItem(Type.create(DotName.createSimple(klass.getName()), Type.Kind.CLASS)));
        }
    }
}
