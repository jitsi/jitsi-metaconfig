package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.MetaconfigSettings

/**
 * Transforms the value of the result of [origSupplier] into some new value.
 *
 */
class ValueTransformingSupplier<ValueType : Any>(
    private val originalSupplier: ConfigValueSupplier<ValueType>,
    private val transformer: (ValueType) -> ValueType
) : ConfigValueSupplier<ValueType> {

    override fun get(): ValueType {
        val originalValue = originalSupplier.get()
        return transformer(originalValue).also {
            MetaconfigSettings.logger.debug {
                "${this::class.simpleName}: transformed retrieved value $originalValue to $it"
            }
        }
    }

    override fun toString(): String = "${this::class.simpleName}: transforming value from $originalSupplier"
}
