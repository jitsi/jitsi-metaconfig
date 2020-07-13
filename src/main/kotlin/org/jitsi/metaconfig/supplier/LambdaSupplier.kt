package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.MetaconfigSettings

class LambdaSupplier<ValueType : Any>(
    private val supplier: () -> ValueType
) : ConfigValueSupplier<ValueType> {

    override fun get(): ValueType {
        return supplier().also {
            MetaconfigSettings.logger.debug {
                "${this::class.simpleName}: retrieved value from lambda"
            }
        }
    }

    override fun toString(): String = "${this::class.simpleName}: retrieving from lambda"
}
