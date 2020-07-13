package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.MetaconfigSettings

/**
 * Converts the type of the result of [originalSupplier] from [OriginalType] to
 * [NewType] using the given [converter] function.
 */
class TypeConvertingSupplier<OriginalType : Any, NewType : Any>(
    private val originalSupplier: ConfigValueSupplier<OriginalType>,
    private val converter: (OriginalType) -> NewType
) : ConfigValueSupplier<NewType> {

    override fun get(): NewType {
        val originalValue = originalSupplier.get()
        return converter(originalValue).also {
            MetaconfigSettings.logger.debug {
                "${this::class.simpleName}: converted retrieved value $originalValue to $it"
            }
        }
    }

    override fun toString(): String = "${this::class.simpleName}: converting value from $originalSupplier"
}
