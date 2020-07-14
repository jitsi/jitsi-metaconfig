package org.jitsi.metaconfig.supplier

/**
 * Transforms the value of the result of [origSupplier] into some new value.
 *
 */
class ValueTransformingSupplier<ValueType : Any>(
    private val originalSupplier: ConfigValueSupplier<ValueType>,
    private val transformer: (ValueType) -> ValueType
) : ConfigValueSupplier<ValueType>() {

    override fun doGet(): ValueType = transformer(originalSupplier.get())

    override fun toString(): String = "${this::class.simpleName}: transforming value from $originalSupplier"
}
