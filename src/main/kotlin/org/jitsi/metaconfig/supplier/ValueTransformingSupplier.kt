package org.jitsi.metaconfig.supplier

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
) : ConfigValueSupplier<ValueType>(noDeprecation()) {

    override fun doGet(): ValueType {
        return transformer(originalSupplier.get()).also {
            MetaconfigSettings.logger.debug {
                "${this::class.simpleName}: Transformed value from $originalSupplier"
            }
        }
    }

    override fun toString(): String = "${this::class.simpleName}: transforming value from $originalSupplier"
}
