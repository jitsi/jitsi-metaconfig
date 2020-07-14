package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.Deprecation
import org.jitsi.metaconfig.noDeprecation

/**
 * Transforms the value of the result of [originalSupplier] into some new value.
 */
class ValueTransformingSupplier<ValueType : Any>(
    private val originalSupplier: ConfigValueSupplier<ValueType>,
    deprecation: Deprecation,
    private val transformer: (ValueType) -> ValueType
) : ConfigValueSupplier<ValueType>(deprecation) {
    constructor(originalSupplier: ConfigValueSupplier<ValueType>, converter: (ValueType) -> ValueType) :
        this(originalSupplier, noDeprecation(), converter)

    override fun doGet(): ValueType = transformer(originalSupplier.get())

    override fun toString(): String = "${this::class.simpleName}: transforming value from $originalSupplier"
}
