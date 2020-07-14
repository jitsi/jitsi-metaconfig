package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.Deprecation
import org.jitsi.metaconfig.notDeprecated

/**
 * Converts the type of the result of [originalSupplier] from [OriginalType] to
 * [NewType] using the given [converter] function.
 */
class TypeConvertingSupplier<OriginalType : Any, NewType : Any>(
    private val originalSupplier: ConfigValueSupplier<OriginalType>,
    deprecation: Deprecation,
    private val converter: (OriginalType) -> NewType
) : ConfigValueSupplier<NewType>(deprecation) {
    constructor(originalSupplier: ConfigValueSupplier<OriginalType>, converter: (OriginalType) -> NewType) :
        this(originalSupplier, notDeprecated(), converter)

    override fun doGet(): NewType = converter(originalSupplier.get())

    override fun toString(): String = "${this::class.simpleName}: converting value from $originalSupplier"
}
