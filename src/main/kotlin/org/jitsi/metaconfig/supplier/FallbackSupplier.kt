package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.ConfigException
import org.jitsi.metaconfig.Deprecation

/**
 * A [ConfigValueSupplier] which searches through multiple [ConfigValueSupplier]s, in order,
 * to find a value.
 *
 * Note that a [FallbackSupplier] is never deprecated, only the suppliers it checks can be,
 * so we also pass [Deprecation.NotDeprecated] to [ConfigValueSupplier].
 */
class FallbackSupplier<ValueType : Any>(
    private val suppliers: List<ConfigValueSupplier<ValueType>>
) : ConfigValueSupplier<ValueType>(Deprecation.NotDeprecated) {

    override fun doGet(): ValueType {
        val exceptions = mutableListOf<ConfigException.UnableToRetrieve>()
        for (supplier in suppliers) {
            try {
                return supplier.get()
            } catch (e: ConfigException.UnableToRetrieve) {
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
