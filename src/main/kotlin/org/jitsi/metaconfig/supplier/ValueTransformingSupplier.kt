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
import org.jitsi.metaconfig.MetaconfigSettings
import org.jitsi.metaconfig.noDeprecation

/**
 * Transforms the value of the result of [originalSupplier] into some new value.
 *
 * Note that it makes no sense for this supplier to be deprecated as it doesn't actually retrieve
 * a value itself, so we always pass [noDeprecation].
 */
class ValueTransformingSupplier<ValueType : Any>(
    private val originalSupplier: ConfigValueSupplier<ValueType>,
    private val transformer: (ValueType) -> ValueType
) : ConfigValueSupplier<ValueType>() {

    override fun doGet(): ValueType {
        try {
            return transformer(originalSupplier.get()).also {
                MetaconfigSettings.logger.debug {
                    "${this::class.simpleName}: Transformed value from $originalSupplier"
                }
            }
        } catch (t: ConfigException) {
            throw t
        } catch (t: Throwable) {
            throw ConfigException.UnableToRetrieve.Error(t)
        }
    }

    override fun withDeprecation(deprecation: Deprecation): ValueTransformingSupplier<ValueType> {
        return ValueTransformingSupplier(originalSupplier.withDeprecation(deprecation), transformer)
    }

    override fun toString(): String = "${this::class.simpleName}: transforming value from $originalSupplier"
}
