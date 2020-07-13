![Java CI with Maven](https://github.com/bbaldino/jitsi-metaconfig/workflows/Java%20CI%20with%20Maven/badge.svg)

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

    // Optional property
    val optional: Boolean? by optionalconfig("

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

### ConfigSource
jitsi-metaconfig defines a `ConfigSource` interface which represents some source of configuration properties.  A `ConfigSource` must implement a `name` (used for logging) and a single method:
```kotlin
fun getterFor(type: KType): (String) -> Any
```
`getterFor` takes in a `KType` and returns a lambda which takes a `String` and returns an `Any` instance.  This lambda is responsible for taking in a key path and returning the value of the configuration property at that key path (or throws an exception if one can't be found).  Any type supported by the underlying configuration library should be supported here.

The interface allows you to plug in the configuration library (or libraries) you're using to jitsi-metaconfig.  Here's an (abridged) example `ConfigSource` for to integrate the `Config` object from the [Typesafe config](https://github.com/lightbend/config) library:

```kotlin
import com.typesafe.config.Config
import org.jitsi.metaconfig.ConfigSource
import java.time.Duration
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

class TypesafeConfigSource(override val name: String, private val config: Config) : ConfigSource {
  override fun getterFor(type: KType): (String) -> Any {
        if (type.isSubtypeOf(typeOf<Enum<*>>())) {
            return getterForEnum(type.classifier as KClass<Nothing>)
        }
        return when (type) {
            typeOf<Boolean>() -> { key -> config.getBoolean(key) }
            typeOf<Int>() -> { key -> config.getInt(key) }
            typeOf<String>() -> { key -> config.getString(key) }
            typeOf<List<String>>() -> { key -> config.getStringList(key) }
            typeOf<List<Int>>() -> { key -> config.getIntList(key) }
            // ...etc
            else -> TODO("no support for type $type")
        }
    }

    private fun<T : Enum<T>> getterForEnum(clazz: KClass<T>): (String) -> T {
        return { key -> config.getEnum(clazz.java, key) }
  }
}
```



#### TODO:
- [ ] Allow marking a property in a specific source as 'deprecated' and warn if a value is used from there
- [ ] Add MetaconfigSetttings option for deprecated property behavior (log a warning, log an error, throw an exception)
- [ ] Allow 'conditional' properties: properties which throw unless a predicate is met (useful if some properties should only be accessed based on being 'enabled' by another property, for example)
