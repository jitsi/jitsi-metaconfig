package org.jitsi.metaconfig

import kotlin.reflect.KType


// TODO: looking surprisingly good, I think.  What remains is QOL-type fixes.
// 1) try more tests.
// 2) try from java.
// 3) look at use cases in jvb and see how they go.
// 3a) There's a use case to want a 'null' return value if a field isn't required.  It doesn't look
//     like we can overload the delegate based on the type being nullable, but we can detect if it's
//     nullable via the KType, so we could split it in the helper function and have a delegate which,
//     if it finds nothing, returns null instead of throwing [DONE]
// 3b) There's a use case for finding a property value, but it fails to parse (or 'validate': one checked
//     for an Int being an 'unprivileged' port.  This would throw an exception, but it should be a sub-class
//     of ConfigPropertyNotFoundException, and bubbled up in some way?
// 3c) Use case for 'conditional property': a property is only set if a predicate is true.  The old code
//     threw, but maybe we'd want to just make it nullable instead?  It would look like NullableConfigDelegate,
//     but take a predicate. (The old code had it throw on access, I think, but we can't do that with the delegate...
//     we could do that if we stored it as a Supplier instead of the actual type?
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
        override fun get(): ValueType = source.getterFor(type)(key) as ValueType
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
        override fun get(): NewType = converter(origSupplier.get())
    }

    /**
     * Transforms the value of the result of [origSupplier] into some new value.
     *
     * The new value may have a different type than the original type.
     */
    class ValueTransformingSupplier<ValueType : Any>(
        private val origSupplier: ConfigValueSupplier<ValueType>,
        private val transformer: (ValueType) -> ValueType
    ) : ConfigValueSupplier<ValueType>() {
        override fun get(): ValueType = transformer(origSupplier.get())
    }

    class FallbackSupplier<ValueType : Any>(
        private val suppliers: List<ConfigValueSupplier<ValueType>>
    ) : ConfigValueSupplier<ValueType>() {
        override fun get(): ValueType {
            for (supplier in suppliers) {
                try {
                    return supplier.get()
                } catch (e: ConfigPropertyNotFoundException) {}
            }
            throw ConfigPropertyNotFoundException("we ain't found shit")
        }
    }
}
