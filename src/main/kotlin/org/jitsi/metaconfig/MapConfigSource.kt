package org.jitsi.metaconfig

import kotlin.reflect.KType

/**
 * An implementation of [ConfigSource] which pulls properties from a map
 */
class MapConfigSource(
    override val name: String,
    private val configValues: Map<String, Any> = mapOf()
) : ConfigSource {
    constructor(name: String, vararg props: Pair<String, Any>) : this(name, props.toMap())
    constructor(name: String, mapBuilder: MutableMap<String, Any>.() -> Unit) : this(name, LinkedHashMap<String, Any>().apply(mapBuilder))

    override fun getterFor(type: KType): (String) -> Any {
        return { configKey ->
            configValues.getOrElse(configKey) { throw ConfigPropertyNotFoundException("key not found") }
        }
    }
}
