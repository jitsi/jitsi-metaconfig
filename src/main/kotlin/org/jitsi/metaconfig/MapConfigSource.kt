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
 */
class MapConfigSource(
    override val name: String,
    private val configValues: MutableMap<String, Any> = mutableMapOf()
) : ConfigSource, MutableMap<String, Any> by configValues {
    constructor(
        name: String,
        mapBuilder: MutableMap<String, Any>.() -> Unit
    ) : this(name, LinkedHashMap<String, Any>().apply(mapBuilder))

    override fun getterFor(type: KType): (String) -> Any {
        return when (type) {
            typeOf<Boolean>() -> getCatching<Boolean>()
            typeOf<Long>() -> getCatching<Long>()
            typeOf<Int>() -> getCatching<Int>()
            typeOf<String>() -> getCatching<String>()
            typeOf<Duration>() -> getCatching<Duration>()
            else -> throw ConfigException.UnsupportedType("Type $type not supported by this source")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Any> getCatching(): (String) -> T {
        return { key ->
            val value = configValues[key] ?: throw ConfigException.UnableToRetrieve.NotFound("not found")
            value as? T ?: throw ConfigException.UnableToRetrieve.WrongType(
                "Wrong type: expected type " +
                    "${typeOf<T>().classifier}, got type ${value::class}"
            )
        }
    }
}
