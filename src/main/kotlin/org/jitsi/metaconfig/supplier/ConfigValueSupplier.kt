package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.ConfigException
import org.jitsi.metaconfig.Deprecation

/**
 * A [ConfigValueSupplier] is a class which is responsible for retrieving the value
 * of a configuration property.
 */
abstract class ConfigValueSupplier<ValueType : Any> {
    private val value: ValueType by lazy { doGet() }

    fun get(): ValueType = value

    // TODO: only 'source' suppliers should implement this.  enforce that with a lower-level
    // abstract class?
    open fun withDeprecation(deprecation: Deprecation): ConfigValueSupplier<ValueType> = this

    /**
     * Get the value from this supplier.  Throws [ConfigException.UnableToRetrieve]
     * if the property wasn't found.
     */
    protected abstract fun doGet(): ValueType
}
