# Hazelcast and Quarkus

<a href="https://github.com/actions/toolkit"><img alt="GitHub Actions status" src="https://github.com/pivovarit/quarkus-hazelcast-client-extension/workflows/build/badge.svg"></a>

## Features
- Lazy init for client (TODO)

### Configuration using `hazelcast.yml`

In order to configure client using the `hazelcast.yml` file, place the configuration file in `src/main/resources` and wire up the following bean:

    @Produces
    ClientConfig createInstance() {
        return new ClientClasspathYamlConfig("hazelcast.yml");
    }
    
Configuration entries from `hazelcast.yml` will override all `quarkus.hazelcast-client.*` entries.

## Limitations
- Default serialization is not supported in native mode
- Configuration via `hazelcast.xml` is not fully supported
