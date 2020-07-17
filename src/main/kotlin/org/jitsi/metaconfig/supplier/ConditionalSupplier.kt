package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.ConfigException
import org.jitsi.metaconfig.noDeprecation

/**
 * A [ConfigValueSupplier] which searches through multiple inner [ConfigValueSupplier]s, in order,
 * *if* the passed [predicate] returns true.  If the predicate returns false, then
 * [ConfigException.UnableToRetrieve.ConditionNotMet] is thrown.
 */
class ConditionalSupplier<ValueType : Any>(
    private val predicate: () -> Boolean,
    innerSuppliers: List<ConfigValueSupplier<ValueType>>
) : ConfigValueSupplier<ValueType>(noDeprecation()) {
    private val innerSupplier = FallbackSupplier(innerSuppliers)

    override fun doGet(): ValueType {
        if (predicate()) {
            return innerSupplier.get()
        }
        else {
            throw ConfigException.UnableToRetrieve.ConditionNotMet("Predicate not met on conditional property")
        }
    }

    override fun toString(): String = "${this::class.simpleName}: predicate wrapper around: $innerSupplier"
}
