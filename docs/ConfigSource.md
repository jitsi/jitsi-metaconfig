# Creating a ConfigSource

jitsi-metaconfig is not a configuration library, but it needs to be able to retrieve configuration values, given some key.  It does this using the `ConfigSource` interface, which acts as a bridge between jitsi-metaconfig and however you're retrieving configuration values under the hood.

A [ConfigSource](src/main/kotlin/org/jitsi/metaconfig/ConfigSource.kt) 
 must implement a user-friendly name, and a single method:
 ```kotlin
 fun getterFor(type: KType): (String) -> Any
 ``` 
 `getterFor` takes in a `KType` and returns a lambda which takes a `String`  (the config property path) and returns an `Any` instance. This lambda is responsible returning the value of the configuration property at configuration property path (or throw an exception if one can't be found). Any type supported by the underlying configuration library should be supported here.

Here is an (abridged) example of implementing a `ConfigSource` when using the [Typesafe config](https://github.com/lightbend/config) library:

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
        return when (type) {
            typeOf<Boolean>() -> { key -> config.getBoolean(key) }
            typeOf<Int>() -> { key -> config.getInt(key) }
            typeOf<String>() -> { key -> config.getString(key) }
            typeOf<List<String>>() -> { key -> config.getStringList(key) }
            typeOf<List<Int>>() -> { key -> config.getIntList(key) }
            // ...etc
            else -> TODO("Type $type not supported")
        }
    }
}
```
There is no set of pre-defined types, so even types specific to one configuration library can be used as long as the `ConfigSource` supports them in `getterFor`.
