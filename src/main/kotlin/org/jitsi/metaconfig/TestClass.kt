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

    // Transformed property
    val bool: Boolean by config {
        retrieve("some.path".from(legacyConfigSource).andTransformBy { !it })
        retrieve("some.new.path".from(newConfigSource))
    }

    // Optional property - returns null if it isn't found anywhere
    val missingPort: Int? by optionalconfig {
        retrieve("some.old.missing.path".from(legacyConfigSource))
        retrieve("some.missing.path".from(newConfigSource))
    }

    // Enum type
    val color: Colors by config {
        retrieve("color".from(newConfigSource).asType<String>().andConvertBy { enumValueOf<Colors>(it) })
    }

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
    println(f.color)
    println(f.interval)
}

// All of the following code would be done in the application repo


enum class Colors {
    ORANGE,
    WHITE,
    BLACK
}

private val newConfigSource = MapConfigSource(
    "new",
    mapOf(
        "app.server.port" to 8080,
        "new.path.interval" to Duration.ofSeconds(5),
        "color" to "ORANGE"
    )
)
private val legacyConfigSource = MapConfigSource(
    "legacy",
    mapOf(
        "old.path.interval.millis" to 7000
    )
)
