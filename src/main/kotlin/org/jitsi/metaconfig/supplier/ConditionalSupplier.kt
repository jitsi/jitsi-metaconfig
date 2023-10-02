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

import org.jitsi.metaconfig.Condition
import org.jitsi.metaconfig.ConfigException
import org.jitsi.metaconfig.Deprecation

/**
 * A [ConfigValueSupplier] which searches through multiple inner [ConfigValueSupplier]s, in order,
 * *if* the passed [Condition] is met.  If the predicate returns false, then
 * [ConfigException.UnableToRetrieve.ConditionNotMet] is thrown.
 */
class ConditionalSupplier<ValueType : Any>(
    private val condition: Condition,
    innerSuppliers: List<ConfigValueSupplier<ValueType>>
) : ConfigValueSupplier<ValueType>() {
    private val innerSupplier = FallbackSupplier(innerSuppliers)

    override fun doGet(): ValueType {
        if (condition.isMet()) {
            return innerSupplier.get()
        } else {
            throw ConfigException.UnableToRetrieve.ConditionNotMet("Property only enabled when: ${condition.context}")
        }
    }

    override fun withDeprecation(deprecation: Deprecation): LambdaSupplier<ValueType> =
        throw Exception("ConditionalSupplier can't be marked as deprecated!")

    override fun toString(): String =
        "${this::class.simpleName}: Enabled only when ${condition.context}: $innerSupplier"
}
