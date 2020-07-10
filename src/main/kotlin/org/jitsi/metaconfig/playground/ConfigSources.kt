package org.jitsi.metaconfig.playground

import org.jitsi.metaconfig.ConfigPropertyNotFoundException
import org.jitsi.metaconfig.ConfigSource
import java.time.Duration
import kotlin.reflect.KType

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
        "old.path.interval" to Duration.ofSeconds(10),
        "old.path.interval.millis" to 7000
    )
)
