# Hazelcast and Quarkus

<a href="https://github.com/actions/toolkit"><img alt="GitHub Actions status" src="https://github.com/pivovarit/quarkus-hazelcast-client-extension/workflows/build/badge.svg"></a>

## Features
- Lazy init for client (TODO)

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
