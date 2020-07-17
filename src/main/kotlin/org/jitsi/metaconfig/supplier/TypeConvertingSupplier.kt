package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.MetaconfigSettings
import org.jitsi.metaconfig.noDeprecation

/**
 * Converts the type of the result of [originalSupplier] from [OriginalType] to
 * [NewType] using the given [converter] function.
 *
 * Note that it makes no sense for this supplier to be deprecated as it doesn't actually retrieve
 * a value itself, so we always pass [noDeprecation].
 */
class TypeConvertingSupplier<OriginalType : Any, NewType : Any>(
    private val originalSupplier: ConfigValueSupplier<OriginalType>,
    private val converter: (OriginalType) -> NewType
) : ConfigValueSupplier<NewType>() {

    override fun doGet(): NewType {
        return converter(originalSupplier.get()).also {
            MetaconfigSettings.logger.debug {
                "${this::class.simpleName}: Converted value type from $originalSupplier"
            }
        }
    }

    override fun toString(): String = "${this::class.simpleName}: converting value from $originalSupplier"
}
