# Hazelcast Client for Quarkus

<a href="https://github.com/actions/toolkit"><img alt="GitHub Actions status" src="https://github.com/pivovarit/quarkus-hazelcast-client-extension/workflows/build/badge.svg"></a>

[Hazelcast IMDG](https://hazelcast.com/products/imdg/) is a distributed in-memory object store and compute engine that supports a wide variety of data structures such as Map, Set, List, MultiMap, RingBuffer, HyperLogLog. 

Use Hazelcast IMDG to store your data in RAM, spread and replicate it across a cluster of machines, and perform data-local computation on it. 

Hazelcast is:
- cloud and Kubernetes friendly
- often used as a Distributed Cache amongst other use cases

## Features
- The HazelcastInstance bean is initialized lazily by Quarkus, if you want eager initialization, make sure to double-check [Quarkus Documentation](https://quarkus.io/guides/cdi-reference#eager-instantiation-of-beans). 

## Quarkus hazelcast-client configuration

By default, client will try to connect to a Hazelcast instance running on the host using port 5701.

Defaults can be customized using `application.properties` entries such as:

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

If you need more configuration options than these, wire-up your own `ClientConfig` or `HazelcastInstance` bean, or fallback to standard `hazelcast-client.yml/hazelcast-client.xml`-based configuration (described below). 

### Configuration Files

The default location for `hazelcast-client.yml` is `src/main/resources`. If you want to use a different one, remember to provide GraalVM with its location (native mode only).

#### Configuration using `hazelcast-client.yml`

In order to configure client using the `hazelcast-client.yml` file, place the configuration file in `src/main/resources` and add the following Quarkus configuration entry:

    quarkus.hazelcast-client.config-source=yaml
    
Configuration entries from `hazelcast-client.yml` override all `quarkus.hazelcast-client.*` entries.

## Limitations
- Default serialization is not supported in native mode