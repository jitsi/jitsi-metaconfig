package org.jitsi.metaconfig

import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A [ConfigSource] which allows modifying the properties after its creation.
 *
 * Useful for testing because the config source can be used by classes but its
 * values changed later.
 */
class MapConfigSource(
    override val name: String,
    private val configValues: MutableMap<String, Any> = mutableMapOf()
) : ConfigSource, MutableMap<String, Any> by configValues {
    constructor(name: String, mapBuilder: MutableMap<String, Any>.() -> Unit) : this(name, LinkedHashMap<String, Any>().apply(mapBuilder))

    override fun getterFor(type: KType): (String) -> Any {
        return when (type) {
            typeOf<Boolean>() -> getCatching<Boolean>()
            typeOf<Long>() -> getCatching<Long>()
            typeOf<Int>() -> getCatching<Int>()
            else -> throw ConfigException.UnsupportedType("Type $type not supported by this source")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Any> getCatching(): (String) -> T {
        return { key ->
            val value = configValues[key] ?: throw ConfigException.UnableToRetrieve.NotFound("not found")
            value as? T ?: throw ConfigException.UnableToRetrieve.WrongType("wrong type")
        }
    }
}
