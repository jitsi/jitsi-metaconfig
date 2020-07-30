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

class LambdaSupplier<ValueType : Any>(
    private val context: String,
    private val supplier: () -> ValueType
) : ConfigValueSupplier<ValueType>() {
    constructor(supplier: () -> ValueType) : this("", supplier)

    override fun doGet(): ValueType {
        MetaconfigSettings.logger.debug {
            "${this::class.simpleName}: Trying to retrieve value via $context"
        }
        return try {
            supplier().also {
                MetaconfigSettings.logger.debug {
                    "$this: found value"
                }
            }
        } catch (e: ConfigException) {
            throw e
        } catch (t: Throwable) {
            throw ConfigException.UnableToRetrieve.Error(t)
        }
    }

    override fun withDeprecation(deprecation: Deprecation): LambdaSupplier<ValueType> =
        throw Exception("LambdaSupplier can't be marked as deprecated!")

    override fun toString(): String = "${this::class.simpleName}${if (context.isNotBlank()) ": '$context'" else ""}"
}
