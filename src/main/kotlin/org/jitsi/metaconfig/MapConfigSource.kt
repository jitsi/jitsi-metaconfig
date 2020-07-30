/*
 * Copyright @ 2018 - present 8x8, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jitsi.metaconfig

import java.time.Duration
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A [ConfigSource] which allows modifying the properties after its creation.
 *
 * Useful for testing because the config source can be used by classes but its
 * values changed later.
 * @param delegate An optional delegate [ConfigSource] which will be used when a key is not found in the map.
 */
class MapConfigSource(
    override val name: String,
    private val configValues: MutableMap<String, Any> = mutableMapOf(),
    private val delegate: ConfigSource? = null
) : ConfigSource, MutableMap<String, Any> by configValues {
    constructor(name: String, mapBuilder: MutableMap<String, Any>.() -> Unit) : this(name, LinkedHashMap<String, Any>().apply(mapBuilder))

    override fun getterFor(type: KType): (String) -> Any {
        return when (type) {
            typeOf<Boolean>() -> getCatching<Boolean>(type)
            typeOf<Long>() -> getCatching<Long>(type)
            typeOf<Int>() -> getCatching<Int>(type)
            typeOf<String>() -> getCatching<String>(type)
            typeOf<Duration>() -> getCatching<Duration>(type)
            else -> throw ConfigException.UnsupportedType("Type $type not supported by this source")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Any> getCatching(type: KType): (String) -> T {
        return { key ->
            val value = configValues[key]
                ?: delegate?.getterFor(type)?.invoke(key)
                ?: throw ConfigException.UnableToRetrieve.NotFound("not found")
            value as? T ?: throw ConfigException.UnableToRetrieve.WrongType(
                "Wrong type: expected type " +
                    "${typeOf<T>().classifier}, got type ${value::class}"
            )
        }
    }
}
