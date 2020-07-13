package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.ConfigException

/**
 * A [ConfigValueSupplier] is a class which is responsible for retrieving the value
 * of a configuration property.
 */
interface ConfigValueSupplier<ValueType : Any> {
    /**
     * Get the value from this supplier.  Throws [ConfigException]
     * if the property wasn't found.
     */
    fun get(): ValueType
}
