package org.jitsi.metaconfig

import kotlin.reflect.KType


// TODO: looking surprisingly good, I think.  What remains is QOL-type fixes.  Right now
// we don't use ConfigResult, but it could be useful to change it to include the source
// in the not-found case so we can give some good errors if something isn't found anywhere?
// look into that and see if it would be useful.  can also get rid of the old attempts since
// everything is in git now.  try more tests.  try from java.  look at use cases in jvb and
// see how they go.  **look at how testing will work**

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
