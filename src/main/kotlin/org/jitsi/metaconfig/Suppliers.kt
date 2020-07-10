package org.jitsi.metaconfig

import kotlin.reflect.KType

sealed class ConfigValueSupplier<ValueType : Any> {
    /**
     * Get the value from this supplier.  Throws [ConfigPropertyNotFoundException]
     * if the property wasn't found.
     */
    abstract fun get(): ValueType

    /**
     * Reads the given [key] from [source] as [type]
     */
    class ConfigSourceSupplier<ValueType : Any>(
        val key: String,
        val source: ConfigSource,
        val type: KType
    ) : ConfigValueSupplier<ValueType>() {

        @Suppress("UNCHECKED_CAST")
        override fun get(): ValueType {
            return source.getterFor(type)(key) as ValueType
        }
    }

    /**
     * Transforms the result of [origSupplier] using the given [transformer]
     * function
     */
    class TransformingSupplier<OriginalType : Any, NewType : Any>(
        private val origSupplier: ConfigValueSupplier<OriginalType>,
        private val transformer: (OriginalType) -> NewType
    ) : ConfigValueSupplier<NewType>() {

        override fun get(): NewType {
            return transformer(origSupplier.get())
        }
    }

    class FallbackSupplier<ValueType : Any>(
        vararg suppliers: ConfigValueSupplier<ValueType>
    ) : ConfigValueSupplier<ValueType>() {
        private val mySuppliers = suppliers.toList()
        override fun get(): ValueType {
            for (supplier in mySuppliers) {
                try {
                    return supplier.get()
                } catch (e: ConfigPropertyNotFoundException) {}
            }
            throw ConfigPropertyNotFoundException("we ain't found shit")
        }
    }
}

/**
 * Transform the value retrieved by the receiving [ConfigValueSupplier] (of type [OriginalType]) to a new
 * value of type [NewType] via the given [transformer] function.
 */
fun <OriginalType : Any, NewType : Any>
ConfigValueSupplier<OriginalType>.transformedBy(transformer: (OriginalType) -> NewType) : ConfigValueSupplier.TransformingSupplier<OriginalType, NewType> {
    return ConfigValueSupplier.TransformingSupplier(this, transformer)
}
