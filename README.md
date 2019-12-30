# Hazelcast and Quarkus

<a href="https://github.com/actions/toolkit"><img alt="GitHub Actions status" src="https://github.com/pivovarit/quarkus-hazelcast-client-extension/workflows/build/badge.svg"></a>

## Features
- The HazelcastInstance bean is initialized lazily by Quarkus, if you want eager initialization, make sure to double-check [Quarkus Documentation](https://quarkus.io/guides/cdi-reference#eager-instantiation-of-beans). 

## Quarkus hazelcast-client configuration

Default Hazelcast Client instance can be configured using `application.properties` entries such as:

    quarkus.hazelcast-client.cluster-members
    quarkus.hazelcast-client.outbound-port-definitions
    quarkus.hazelcast-client.outbound-ports
    quarkus.hazelcast-client.labels
    quarkus.hazelcast-client.group-name
    quarkus.hazelcast-client.connection-attempt-limit
    quarkus.hazelcast-client.connection-attempt-period
    quarkus.hazelcast-client.connection-timeout
    quarkus.hazelcast-client.executor-pool-size

All of them mirror standard Hazelcast Client configuration options.

If you need more configuration options than these, wire-up your own `ClientConfig` or `HazelcastInstance` bean, or fallback to standard `hazelcast.yml/hazelcast.xml`-based configuration (described below). 

### Configuration using `hazelcast.yml`

In order to configure client using the `hazelcast.yml` file, place the configuration file in `src/main/resources` and add the following Quarkus configuration entry:

    quarkus.hazelcast-client.config-source=yaml
    
### Configuration using `hazelcast.xml` (limited supported in native mode)

In order to configure client using the `hazelcast.xml` file, place the configuration file in `src/main/resources` and add the following Quarkus configuration entry:

    quarkus.hazelcast-client.config-source=xml
    
Configuration entries from `hazelcast.xml` will override all `quarkus.hazelcast-client.*` entries.

## Limitations
- Default serialization is not supported in native mode
- Configuration via `hazelcast.xml` is not fully supported
