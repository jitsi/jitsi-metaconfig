package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.Deprecation

class LambdaSupplier<ValueType : Any>(
    private val context: String,
    deprecation: Deprecation,
    private val supplier: () -> ValueType
) : ConfigValueSupplier<ValueType>(deprecation) {
    constructor(supplier: () -> ValueType) : this("", Deprecation.NotDeprecated, supplier)
    constructor(deprecation: Deprecation, supplier: () -> ValueType) : this("", deprecation, supplier)
    constructor(context: String, supplier: () -> ValueType) : this(context, Deprecation.NotDeprecated, supplier)

    override fun doGet(): ValueType = supplier()

    override fun toString(): String = "${this::class.simpleName} $context"
}
