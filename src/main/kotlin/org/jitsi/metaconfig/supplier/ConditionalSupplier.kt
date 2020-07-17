package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.Condition
import org.jitsi.metaconfig.ConfigException

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

    override fun toString(): String = "${this::class.simpleName}: Enabled only when ${condition.context}: $innerSupplier"
}
