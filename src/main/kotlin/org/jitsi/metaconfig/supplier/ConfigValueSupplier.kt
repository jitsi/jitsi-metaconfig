package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.ConfigException
import org.jitsi.metaconfig.MetaconfigSettings

/**
 * A [ConfigValueSupplier] is a class which is responsible for retrieving the value
 * of a configuration property.
 */
abstract class ConfigValueSupplier<ValueType : Any> {
    fun get(): ValueType {
        return doGet().also {
            MetaconfigSettings.logger.debug {
                "${this::class.simpleName}: value found via $this"
            }
        }
    }
    /**
     * Get the value from this supplier.  Throws [ConfigException]
     * if the property wasn't found.
     */
    protected abstract fun doGet(): ValueType
}
