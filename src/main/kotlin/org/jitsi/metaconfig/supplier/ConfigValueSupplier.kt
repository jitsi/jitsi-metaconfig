package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.ConfigException
import org.jitsi.metaconfig.Deprecation
import org.jitsi.metaconfig.MetaconfigSettings

/**
 * A [ConfigValueSupplier] is a class which is responsible for retrieving the value
 * of a configuration property.
 */
abstract class ConfigValueSupplier<ValueType : Any>(
    private val deprecation: Deprecation
) {
    fun get(): ValueType {
        return doGet().also {
            MetaconfigSettings.logger.debug {
                "${this::class.simpleName}: value found via $this"
            }
            if (deprecation is Deprecation.Deprecated.Soft) {
                MetaconfigSettings.logger.warn {
                    "A value was retrieved via $this which is deprecated: ${deprecation.msg}"
                }
            } else if (deprecation is Deprecation.Deprecated.Hard) {
                throw ConfigException.UnableToRetrieve.Deprecated(
                    "A value was retrieved via $this which is deprecated: ${deprecation.msg}"
                )
            }
        }
    }
    /**
     * Get the value from this supplier.  Throws [ConfigException]
     * if the property wasn't found.
     */
    protected abstract fun doGet(): ValueType
}
