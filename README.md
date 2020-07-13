# jitsi-metaconfig

jitsi-metaconfig is _not_ a config library.  jitsi-metaconfig helps solve the problems around dealing with configuration properties, such as:

1) Property keys changing
1) Changing property file locations or formats
1) The value type of a property key changing

jitsi-metaconfig allows defining properties in code in such a way that names, locations and types can all be changed while still supporting the old format so deployments which use old keys/types/files won't break.

Example config properties:
```kotlin
class Foo {
    // Simple property
    val enabled: Boolean by config("app.enabled").from(myConfigSource))

	// Convert the type
	val interval: Duration by config {
	    retrieve("app.interval".from(myConfigSource).asType<Long>().andConvertBy(Duration::ofMillis))
	}

	// Transform the value
	val enabled: Boolean by config {
	    retrieve("app.disabled".from(myConfigSource).andTransformBy { !it })
	}

	// Search for value in a legacy config file and then the new one
	val enabled: Boolean by config {
	    retrieve("old.path.app.enabled".from(legacyConfigSource))
	    retrieve("new.path.app.enabled".from(newConfigSource))
	}
}
```
