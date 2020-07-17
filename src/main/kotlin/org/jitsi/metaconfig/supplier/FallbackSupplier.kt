package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.ConfigException
import org.jitsi.metaconfig.MetaconfigSettings
import org.jitsi.metaconfig.noDeprecation

/**
 * A [ConfigValueSupplier] which searches through multiple [ConfigValueSupplier]s, in order,
 * to find a value.
 *
 * Note that a [FallbackSupplier] is never deprecated, only the suppliers it checks can be,
 * so we also pass [noDeprecation] to [ConfigValueSupplier].
 */
class FallbackSupplier<ValueType : Any>(
    private val suppliers: List<ConfigValueSupplier<ValueType>>
) : ConfigValueSupplier<ValueType>() {

    override fun doGet(): ValueType {
        MetaconfigSettings.logger.debug {
            "${this::class.simpleName}: checking for value via suppliers:" +
                suppliers.joinToString(prefix = "\n  ", separator = "\n  ")
        }
        val exceptions = mutableListOf<ConfigException.UnableToRetrieve>()
        for (supplier in suppliers) {
            try {
                return supplier.get().also {
                    MetaconfigSettings.logger.debug {
                        "${this::class.simpleName}: value found via $supplier"
                    }
                }
            } catch (e: ConfigException.UnableToRetrieve) {
                MetaconfigSettings.logger.debug {
                    "${this::class.simpleName}: failed to find value via $supplier: $e"
                }
                exceptions += e
            }
        }
        throw ConfigException.UnableToRetrieve.NotFound(
            "No suppliers found a value:${exceptions.joinToString(prefix = "\n  ", separator = "\n  ")}"
        )
    }

    override fun toString(): String = "${this::class.simpleName}: checking suppliers:" +
        suppliers.joinToString(prefix = "\n  ", separator = "\n  ")
}
