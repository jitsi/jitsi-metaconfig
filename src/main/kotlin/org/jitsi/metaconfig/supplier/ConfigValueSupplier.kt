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
    private var deprecationWarningLogged = false

    private val value: ValueType by lazy {
        doGet().also {
            if (deprecation is Deprecation.Deprecated.Soft && !deprecationWarningLogged) {
                MetaconfigSettings.logger.warn {
                    "A value was retrieved via $this which is deprecated: ${deprecation.msg}"
                }
                deprecationWarningLogged = true
            } else if (deprecation is Deprecation.Deprecated.Hard) {
                throw ConfigException.UnableToRetrieve.Deprecated(
                    "A value was retrieved via $this which is deprecated: ${deprecation.msg}"
                )
            }
        }
    }

    fun get(): ValueType = value

    /**
     * Get the value from this supplier.  Throws [ConfigException.UnableToRetrieve]
     * if the property wasn't found.
     */
    protected abstract fun doGet(): ValueType
}
