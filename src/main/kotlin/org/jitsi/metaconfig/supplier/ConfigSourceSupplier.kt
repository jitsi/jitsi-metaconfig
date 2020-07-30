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

package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.ConfigException
import org.jitsi.metaconfig.ConfigSource
import org.jitsi.metaconfig.Deprecation
import org.jitsi.metaconfig.MetaconfigSettings
import kotlin.reflect.KType

/**
 * Retrieve the given [key] from [source] as [type], where [type] and [ValueType] must 'match'
 */
class ConfigSourceSupplier<ValueType : Any>(
    private val key: String,
    private val source: ConfigSource,
    private val type: KType,
    private val deprecation: Deprecation
) : ConfigValueSupplier<ValueType>() {
    private var deprecationWarningLogged = false

    @Suppress("UNCHECKED_CAST")
    override fun doGet(): ValueType {
        MetaconfigSettings.logger.debug {
            "${this::class.simpleName}: Trying to retrieve key '$key' from source '${source.name}' as type $type"
        }
        try {
            return (source.getterFor(type)(key) as ValueType).also {
                MetaconfigSettings.logger.debug {
                    "${this::class.simpleName}: Successfully retrieved key '$key' from source '${source.name}' as type $type"
                }
                if (deprecation is Deprecation.Deprecated.Soft && !deprecationWarningLogged) {
                    MetaconfigSettings.logger.warn {
                        "Key '$key' from source '${source.name}' is deprecated: ${deprecation.msg}"
                    }
                    deprecationWarningLogged = true
                } else if (deprecation is Deprecation.Deprecated.Hard) {
                    throw ConfigException.UnableToRetrieve.Deprecated(
                        "Key '$key' from source '${source.name}' is deprecated: ${deprecation.msg}"
                    )
                }
            }
        } catch (t: ConfigException.UnableToRetrieve) {
            throw t
        } catch (t: Throwable) {
            throw ConfigException.UnableToRetrieve.Error(t)
        }
    }

    /**
     * Return a new [ConfigSourceSupplier] with the same key, source and deprecation but which
     * retrieves as [newType] instead of [type].
     */
    fun <NewType : Any> withRetrievedType(newType: KType): ConfigSourceSupplier<NewType> =
        ConfigSourceSupplier(key, source, newType, deprecation)

    override fun withDeprecation(deprecation: Deprecation): ConfigValueSupplier<ValueType> =
        ConfigSourceSupplier(key, source, type, deprecation)

    override fun toString(): String = "${this::class.simpleName}: key: '$key', type: '$type', source: '${source.name}'"
}
