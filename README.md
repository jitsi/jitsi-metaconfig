![Java CI with Maven](https://github.com/bbaldino/jitsi-metaconfig/workflows/Java%20CI%20with%20Maven/badge.svg)

# jitsi-metaconfig

jitsi-metaconfig is _not_ a config library.  jitsi-metaconfig helps solve the problems around the evolution of configuration properties, such as:

1) Property names changing
1) Changing property file locations or formats
1) The value type of a property key changing (e.g. it was a number of milliseconds but is now a Duration)

jitsi-metaconfig allows defining properties in code in such a way that names, locations and types can all be changed while still supporting the old format so deployments which use old keys/types/files won't break.  It also
supports marking old properties as deprecated to ease the transition to removing support for them.

### Example config properties:
```kotlin
class Foo {
    // Simple property
    val enabled: Boolean by config("app.enabled".from(myConfigSource))

    // Optional property
    val optionalParam: String? by optionalconfig("app.client.optional-param".from(myConfigSource))

    // Convert the type (retrieve as a Long, convert to a Duration)
    val interval: Duration by config {
        retrieve("app.interval-ms".from(myConfigSource).asType<Long>().andConvertBy(Duration::ofMillis))
    }

    // Transform the value (invert the retrieved boolean value)
    val enabled: Boolean by config {
        retrieve("app.disabled".from(myConfigSource).andTransformBy { !it })
    }

    // Search for value in a legacy config file and then the new one
    val enabled: Boolean by config {
        retrieve("old.path.app.enabled".from(legacyConfigSource))
        retrieve("new.path.app.enabled".from(newConfigSource))
    }

    // Search for value in a legacy config file and then the new one, mark the old one as deprecated
    val enabled: Boolean by config {
        retrieve("old.path.app.enabled".from(legacyConfigSource).softDeprecated("use 'new.path.app.enabled' in new config source")
        retrieve("new.path.app.enabled".from(newConfigSource))
    }
}
```

Read more:

- [Implementing a ConfigSource](docs/ConfigSource.md)
- [Using property deleagtes to define properties in code](docs/DelegateHelpers.md)
- [SupplierTypes](docs/SupplierTypes.md)

#### TODO:
- [ ] Allow 'conditional' properties: properties which throw unless a predicate is met (useful if some properties should only be accessed based on being 'enabled' by another property, for example)
