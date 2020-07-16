package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.Deprecation
import org.jitsi.metaconfig.MetaconfigSettings

class LambdaSupplier<ValueType : Any>(
    private val context: String,
    deprecation: Deprecation,
    private val supplier: () -> ValueType
) : ConfigValueSupplier<ValueType>(deprecation) {
    constructor(supplier: () -> ValueType) : this("", Deprecation.NotDeprecated, supplier)
    constructor(deprecation: Deprecation, supplier: () -> ValueType) : this("", deprecation, supplier)
    constructor(context: String, supplier: () -> ValueType) : this(context, Deprecation.NotDeprecated, supplier)

    override fun doGet(): ValueType {
        MetaconfigSettings.logger.debug {
            "${this::class.simpleName}: Trying to retrieve value via $context"
        }
        return supplier().also {
            MetaconfigSettings.logger.debug {
                "$this: found value"
            }
        }
    }

    override fun toString(): String = "${this::class.simpleName}${if (context.isNotBlank()) ": '$context'" else ""}"
}
