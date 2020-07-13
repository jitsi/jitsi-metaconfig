package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.ConfigException
import org.jitsi.metaconfig.MetaconfigSettings

/**
 * A [ConfigValueSupplier] which searches through multiple [ConfigValueSupplier]s, in order,
 * to find a value.
 */
class FallbackSupplier<ValueType : Any>(
    private val suppliers: List<ConfigValueSupplier<ValueType>>
) : ConfigValueSupplier<ValueType> {

    override fun get(): ValueType {
        val exceptions = mutableListOf<ConfigException.UnableToRetrieve>()
        for (supplier in suppliers) {
            try {
                return supplier.get().also {
                    MetaconfigSettings.logger.debug {
                        "${this::class.simpleName}: value found from supplier $supplier"
                    }
                }
            } catch (e: ConfigException.UnableToRetrieve) {
                exceptions += e
                MetaconfigSettings.logger.debug {
                    "${this::class.simpleName}: unable to retrieve value from supplier $supplier: $e"
                }
            }
        }
        throw ConfigException.UnableToRetrieve.NotFound(
            "No suppliers found a value:\n${exceptions.joinToString(separator = "\n")}"
        )
    }

    override fun toString(): String = "${this::class.simpleName}: checking suppliers:\n" +
        suppliers.joinToString(separator = "\n")
}
