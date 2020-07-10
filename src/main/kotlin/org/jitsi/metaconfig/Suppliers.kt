package org.jitsi.metaconfig

import kotlin.reflect.KType


// TODO: looking surprisingly good, I think.  What remains is QOL-type fixes.
// 1) try more tests.
// 2) try from java.
// 3) look at use cases in jvb and see how they go.
// 4) **look at how testing will work**

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
        private val key: String,
        private val source: ConfigSource,
        private val type: KType
    ) : ConfigValueSupplier<ValueType>() {

        @Suppress("UNCHECKED_CAST")
        override fun get(): ValueType {
            return source.getterFor(type)(key) as ValueType
        }
    }

    /**
     * Converts the type of the result of [origSupplier] from [OriginalType] to
     * [NewType] using the given [converter] function.
     *
     * TODO: is there a use case for a 'transformer' which transforms the value but
     * doesn't change the type?
     */
    class TypeConvertingSupplier<OriginalType : Any, NewType : Any>(
        private val origSupplier: ConfigValueSupplier<OriginalType>,
        private val converter: (OriginalType) -> NewType
    ) : ConfigValueSupplier<NewType>() {

        override fun get(): NewType {
            return converter(origSupplier.get())
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
ConfigValueSupplier<OriginalType>.convertedBy(transformer: (OriginalType) -> NewType) : ConfigValueSupplier.TypeConvertingSupplier<OriginalType, NewType> {
    return ConfigValueSupplier.TypeConvertingSupplier(this, transformer)
}
