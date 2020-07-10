package org.jitsi.metaconfig

import java.time.Duration
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@ExperimentalStdlibApi
class Foo {
    // Simple property
    val port: Int by config("app.server.port")

    // Fallback property, old config requires transformation of type
    val interval: Duration by config(
        legacyconfig<Long>("old.path.interval.millis").transformedBy(Duration::ofMillis),
        newconfig("new.path.interval")
    )

    val otherPort: Int = ConfigValueSupplier.ConfigSourceSupplier<Int>(
        "app.server.port",
        newConfigSource,
        typeOf<Int>()
    ).get()

    val fallback: Int = ConfigValueSupplier.FallbackSupplier(
        legacyconfig("some.old.path"),
        newconfig<Duration>("some.new.path").transformedBy { it.toMillis().toInt() }
    ).get()

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
}

val newConfigSource = MapConfigSource(
    mapOf(
        "app.server.port" to 8080,
        "new.path.interval" to Duration.ofSeconds(5)
    )
)
val legacyConfigSource = MapConfigSource(
    mapOf(
        "old.path.interval.millis" to 7000
    )
)

@ExperimentalStdlibApi
private inline fun <reified T : Any> config(keyPath: String): ConfigDelegate<T> {
    return ConfigDelegate<T>(ConfigValueSupplier.ConfigSourceSupplier(keyPath, newConfigSource, typeOf<T>()))
}

@ExperimentalStdlibApi
private inline fun <reified T : Any> legacyconfig(keyPath: String): ConfigValueSupplier.ConfigSourceSupplier<T> =
    ConfigValueSupplier.ConfigSourceSupplier(keyPath, legacyConfigSource, typeOf<T>())

@ExperimentalStdlibApi
private inline fun <reified T : Any> newconfig(keyPath: String): ConfigValueSupplier.ConfigSourceSupplier<T> =
    ConfigValueSupplier.ConfigSourceSupplier(keyPath, newConfigSource, typeOf<T>())
