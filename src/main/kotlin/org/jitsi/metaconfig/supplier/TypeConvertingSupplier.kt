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
 * Converts the type of the result of [originalSupplier] from [OriginalType] to
 * [NewType] using the given [converter] function.
 *
 * Note that it makes no sense for this supplier to be deprecated as it doesn't actually retrieve
 * a value itself, so we always pass [noDeprecation].
 */
class TypeConvertingSupplier<OriginalType : Any, NewType : Any>(
    private val originalSupplier: ConfigValueSupplier<OriginalType>,
    private val converter: (OriginalType) -> NewType
) : ConfigValueSupplier<NewType>() {

    override fun doGet(): NewType {
        try {
            return converter(originalSupplier.get()).also {
                MetaconfigSettings.logger.debug {
                    "${this::class.simpleName}: Converted value type from $originalSupplier"
                }
            }
        } catch (t: ConfigException) {
            throw t
        } catch (t: Throwable) {
            throw ConfigException.UnableToRetrieve.Error(t)
        }
    }

    override fun withDeprecation(deprecation: Deprecation): TypeConvertingSupplier<OriginalType, NewType> {
        return TypeConvertingSupplier(originalSupplier.withDeprecation(deprecation), converter)
    }

    override fun toString(): String = "${this::class.simpleName}: converting value from $originalSupplier"
}
