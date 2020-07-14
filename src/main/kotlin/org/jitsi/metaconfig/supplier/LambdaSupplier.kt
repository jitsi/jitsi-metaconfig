package org.jitsi.metaconfig.supplier

class LambdaSupplier<ValueType : Any>(
    private val supplier: () -> ValueType
) : ConfigValueSupplier<ValueType>() {

    override fun doGet(): ValueType = supplier()

    override fun toString(): String = "${this::class.simpleName}"
}
