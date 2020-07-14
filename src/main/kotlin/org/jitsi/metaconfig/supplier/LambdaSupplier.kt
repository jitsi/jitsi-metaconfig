package org.jitsi.metaconfig.supplier

class LambdaSupplier<ValueType : Any>(
    private val context: String,
    private val supplier: () -> ValueType
) : ConfigValueSupplier<ValueType>() {
    constructor(supplier: () -> ValueType) : this("", supplier)

    override fun doGet(): ValueType = supplier()

    override fun toString(): String = "${this::class.simpleName} $context"
}
