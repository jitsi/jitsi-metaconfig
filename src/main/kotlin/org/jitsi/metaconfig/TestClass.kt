package org.jitsi.metaconfig

import java.time.Duration
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@ExperimentalStdlibApi
class Foo {
    // Simple property
//    val port: Int by config(newConfigSource, "app.server.port")
    val port: Int by config("app.server.port".from(newConfigSource))

    // Fallback property, old config requires transformation of type
//    val interval: Duration by config(
//        from<Long>(legacyConfigSource, "old.path.interval.millis").convertedBy(Duration::ofMillis),
//        newconfig("new.path.interval")
//    )
    val interval: Duration by config(
        "old.path.interval.millis".from(legacyConfigSource).asType<Long>().andConvertBy(Duration::ofMillis),
        "new.path.interval".from(newConfigSource).asType<Duration>()
    )

//    val bool: Boolean by config(
//       // TODO: can this be done without having to pass the <Boolean> to from?
//        // I think it's needed because: if it's the only statement, then it's also the result
//        // of the expression and we know the result has to be ConfigValueSupplier<Boolean>.  When
//        // a call is chained onto it, it's not the result anymore so its type can't be inferred
//        from<Boolean>(legacyConfigSource, "some.path").transformedBy { !it },
//        from(newConfigSource, "some.new.path")
//    )
    val bool: Boolean by config(
        // TODO: can we get rid of the need for 'asType' here? (everywhere when using a delegate?)
        // --> we can add states that are without the type, and the inline function can add the type
        // the inline function would take all incomplete state, and test each one and add the type where
        // needed
        "some.path".from(legacyConfigSource).asType<Boolean>().andTransformBy { !it },
        "some.new.path".from(newConfigSource).asType<Boolean>()
    )

    // Optional property - returns null if it isn't found anywhere
    val missingPort: Int? by optionalconfig(
        legacyconfig("some.old.missing.path"),
        newconfig("some.missing.path")
    )

    // Parsing an enum type
    // From what I've found, I need the speciifc 'enumFrom' type, as the enum code has to go
    // through a different path which bounds a generic type via T : Enum<T>.
    // If i can find a way to create an enum from a KType and a String, then this can be done
    // in the overridden 'getterFor' method.
    // TODO: can I mix suppliers here?  I.e. could I grab it as a string and convert to an enum?
    val enum: Colors by config(
        enumFrom(newConfigSource, "color"),
        // If an enum had a value removed and we needed to translate it to another value, could do
        // this
        from<String>(legacyConfigSource, "old.path").convertedBy {
            Colors.ORANGE
        }
    )

//    val fallback: Int = ConfigValueSupplier.FallbackSupplier(
//        legacyconfig("some.old.path"),
//        newconfig<Duration>("some.new.path").convertedBy { it.toMillis().toInt() }
//    ).get()

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
    println(f.enum)
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

