package org.jitsi.metaconfig.supplier

/**
 * Converts the type of the result of [originalSupplier] from [OriginalType] to
 * [NewType] using the given [converter] function.
 */
class TypeConvertingSupplier<OriginalType : Any, NewType : Any>(
    private val originalSupplier: ConfigValueSupplier<OriginalType>,
    private val converter: (OriginalType) -> NewType
) : ConfigValueSupplier<NewType>() {

    override fun doGet(): NewType = converter(originalSupplier.get())

    override fun toString(): String = "${this::class.simpleName}: converting value from $originalSupplier"
}
