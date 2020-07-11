package org.jitsi.metaconfig

import java.time.Duration
import kotlin.reflect.KType

@ExperimentalStdlibApi
class Foo {
    // Simple property
    val port: Int by config("app.server.port".from(newConfigSource))

    // Fallback property, old config requires transformation of type
    val interval: Duration by config {
        retrieve("old.path.interval.millis".from(legacyConfigSource).asType<Long>().andConvertBy(Duration::ofMillis))
        retrieve("new.path.interval".from(newConfigSource))
    }

    // Transformed properrty
    val bool: Boolean by config {
        retrieve("some.path".from(legacyConfigSource).andTransformBy { !it })
        retrieve("some.new.path".from(newConfigSource))
    }

    // Optional property - returns null if it isn't found anywhere
    val missingPort: Int? by optionalconfig {
        retrieve("some.old.missing.path".from(legacyConfigSource))
        retrieve("some.missing.path".from(newConfigSource))
    }

    // Parsing an enum type
    // From what I've found, I need the speciifc 'enumFrom' type, as the enum code has to go
    // through a different path which bounds a generic type via T : Enum<T>.
    // If i can find a way to create an enum from a KType and a String, then this can be done
    // in the overridden 'getterFor' method.
    // TODO: can I mix suppliers here?  I.e. could I grab it as a string and convert to an enum?
//    val enum: Colors by config(
//        enumFrom(newConfigSource, "color"),
//        // If an enum had a value removed and we needed to translate it to another value, could do
//        // this
//        from<String>(legacyConfigSource, "old.path").convertedBy {
//            Colors.ORANGE
//        }
//    )

    // If we change the helpers to plug in the type automatically, then we can implement enums by having the
    // supplier grab them as a string and then converting--it's a little sneaky (it would be confusing if someone saw
    // the type as string when they expected enum), but it would be simple.
    // another option is to build a proper supplier around the enum type, but have the enumconfig helper create its
    // _own_ supplier using the key and source, but grab it as a string (we'd have to also apply any transformers
    // on the original supplier--which actually would be awkward in option 1, so this is probably best)
//    val color: Colors by enumconfig(
//        "color".from(newConfigSource)
//    )

//    // Deprecated - do we care about this?  I would like to at least make sure it's doable.
//    private val yetAnotherInterval: Duration by config(
//        legacyConfig("old.path.interval", deprecated = true),
//        newconfig("new.path.interval")
//    )
}

@ExperimentalStdlibApi
fun main() {
    val f = Foo()
    println(f.port)
    println(f.missingPort)
//    println(f.enum)
    println(f.interval)
}

// All of the following code would be done in the application repo

class MapConfigSource(private val configValues: Map<String, Any> = mapOf()) : ConfigSource {
    override val name: String = "map"

    override fun getterFor(type: KType): (String) -> Any {
        return { configKey ->
            configValues.getOrElse(configKey) { throw ConfigPropertyNotFoundException("key not found") }
        }
    }

    override fun <T : Enum<T>> getterFor(enumClazz: Class<T>): (String) -> T {
        TODO("Not yet implemented")
    }
}

enum class Colors {
    ORANGE,
    WHITE,
    BLACK
}

private val newConfigSource = MapConfigSource(
    mapOf(
        "app.server.port" to 8080,
        "new.path.interval" to Duration.ofSeconds(5),
        "color" to "ORANGE"
    )
)
private val legacyConfigSource = MapConfigSource(
    mapOf(
        "old.path.interval.millis" to 7000
    )
)


// Supplier helpers


@ExperimentalStdlibApi
private inline fun <reified T : Any> legacyconfig(keyPath: String): ConfigValueSupplier.ConfigSourceSupplier<T> =
    from(legacyConfigSource, keyPath)

@ExperimentalStdlibApi
private inline fun <reified T : Any> newconfig(keyPath: String): ConfigValueSupplier.ConfigSourceSupplier<T> =
    from(newConfigSource, keyPath)

