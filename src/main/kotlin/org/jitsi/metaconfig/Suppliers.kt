package org.jitsi.metaconfig

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


// TODO: looking surprisingly good, I think.  What remains is QOL-type fixes.
// 1) try more tests.
// 2) try from java.
// 3) look at use cases in jvb and see how they go.
// 3a) There's a use case to want a 'null' return value if a field isn't required.  It doesn't look
//     like we can overload the delegate based on the type being nullable, but we can detect if it's
//     nullable via the KType, so we could split it in the helper function and have a delegate which,
//     if it finds nothing, returns null instead of throwing
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
     * Enums require a special supplier which takes a type T bounded by Enum<T>,
     * otherwise we can't construct a Class<T> that is bounded correctly to
     * pass to [java.lang.Enum.valueOf].
     */
    @ExperimentalStdlibApi
    class ConfigSourceEnumSupplier<T : Enum<T>>(
        private val key: String,
        private val source: ConfigSource,
        private val clazz: KClass<T>
    ) : ConfigValueSupplier<T>() {
        override fun get(): T = source.getterFor(clazz.java)(key)
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
        suppliers: List<ConfigValueSupplier<ValueType>>
    ) : ConfigValueSupplier<ValueType>() {
        constructor(vararg suppliers: ConfigValueSupplier<ValueType>) : this(suppliers.toList())
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
 * Convert the value retrieved by the receiving [ConfigValueSupplier] (of type [OriginalType]) to a new
 * value of type [NewType] via the given [converter] function.
 */
fun <OriginalType : Any, NewType : Any>
ConfigValueSupplier<OriginalType>.convertedBy(converter: (OriginalType) -> NewType) : ConfigValueSupplier.TypeConvertingSupplier<OriginalType, NewType> {
    return ConfigValueSupplier.TypeConvertingSupplier(this, converter)
}

/**
 * Transform the value retrieved by the receiving [ConfigValueSupplier] to a new value of the same type.
 */
inline fun <ValueType : Any>
ConfigValueSupplier<ValueType>.transformedBy(noinline transformer: (ValueType) -> ValueType) : ConfigValueSupplier.ValueTransformingSupplier<ValueType> {
    return ConfigValueSupplier.ValueTransformingSupplier(this, transformer)
}

// Doesn't help
//inline infix fun <ValueType : Any>
//        ConfigValueSupplier<ValueType>.transformedBy2(noinline transformer: (ValueType) -> ValueType) : ConfigValueSupplier.TransformingSupplier2<ValueType> {
//    return ConfigValueSupplier.TransformingSupplier2(this, transformer)
//}

/**
 * Helper to create a supplier from a [ConfigSource] and a [keyPath]
 */
@ExperimentalStdlibApi
inline fun <reified T : Any> from(configSource: ConfigSource, keyPath: String): ConfigValueSupplier.ConfigSourceSupplier<T> =
    ConfigValueSupplier.ConfigSourceSupplier(keyPath, configSource, typeOf<T>())

@ExperimentalStdlibApi
inline fun <reified T : Any> fromWithClass(configSource: ConfigSource, keyPath: String, valueType: KClass<T>): ConfigValueSupplier.ConfigSourceSupplier<T> =
    ConfigValueSupplier.ConfigSourceSupplier(keyPath, configSource, typeOf<T>())

/**
 * Parsing enum types requires a special method, as Enums require a special code path to extract correctly.
 *
 * We need to have a type T that is bounded via T : Enum<T>
 */
@ExperimentalStdlibApi
inline fun <reified T : Enum<T>> enumFrom(configSource: ConfigSource, keyPath: String): ConfigValueSupplier.ConfigSourceEnumSupplier<T> =
    ConfigValueSupplier.ConfigSourceEnumSupplier(keyPath, configSource, T::class)
