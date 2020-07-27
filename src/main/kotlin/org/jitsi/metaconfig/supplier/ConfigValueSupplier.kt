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
import org.jitsi.metaconfig.Deprecation

/**
 * A [ConfigValueSupplier] is a class which is responsible for retrieving the value
 * of a configuration property.
 */
abstract class ConfigValueSupplier<ValueType : Any> {
    private val value: ValueType by lazy { doGet() }

    fun get(): ValueType = value

    /**
     * Apply a [Deprecation] to this [ConfigValueSupplier].  Deprecation is only applied to types
     * where it makes sense (those which retrieve a value from an 'external' location--right now
     * only [ConfigSourceSupplier]), however, types which wrap an inner supplier must pass the
     * deprecation 'down' so it can be applied correctly by any supplier which should observe it.
     */
    abstract fun withDeprecation(deprecation: Deprecation): ConfigValueSupplier<ValueType>

    /**
     * Get the value from this supplier.  Throws [ConfigException.UnableToRetrieve]
     * if the property wasn't found.
     */
    protected abstract fun doGet(): ValueType
}
