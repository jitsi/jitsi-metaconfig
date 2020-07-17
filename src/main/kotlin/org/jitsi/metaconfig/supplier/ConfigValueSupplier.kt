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

    /**
     * Apply a [Deprecation] to this [ConfigValueSupplier].  By default it does nothing.  This should
     * only be overridden by classes which retrieve properties from some "source" (e.g. a file).
     * Suppliers which wrap another an do some kind of transformation, for example,
     * shouldn't override this.
     *
     */
    open fun withDeprecation(deprecation: Deprecation): ConfigValueSupplier<ValueType> = this

    /**
     * Get the value from this supplier.  Throws [ConfigException.UnableToRetrieve]
     * if the property wasn't found.
     */
    protected abstract fun doGet(): ValueType
}
