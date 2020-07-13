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
//     of ConfigPropertyNotFoundException, and bubbled up in some way? [DONE]
// 3c) Use case for 'conditional property': a property is only set if a predicate is true.  The old code
//     threw, but maybe we'd want to just make it nullable instead?  It would look like NullableConfigDelegate,
//     but take a predicate. (The old code had it throw on access, I think, but we can't do that with the delegate...
//     we could do that if we stored it as a Supplier instead of the actual type?
// 3d) Pulling a value from a class instance: need this for Jibri which parsed the old config into a class
//     instance
// 3e) There's a case like this:
//       val enabled: Boolean by config("enabled".from(configSrc).andTransformBy { !it })
//     that doesn't currently work but I think it could...we could have an Incomplete builder
//     state that had only the type (inferred from the config helper)
// 4) **look at how testing will work**

/**
 * [ConfigValueSupplier]s are classes which define a 'get' method to retrieve
 * the value of a config property.
 *
 * NOTE: It would be easy to get rid of all of these and just use a lambda which
 * gave a value T, but I think these may be useful for adding more information
 * when debugging (each step of retrieval can be logged with interesting
 * information like the source and type); if that doesn't end up being the case then
 * get rid of all these.
 */
sealed class ConfigValueSupplier<ValueType : Any> {
    /**
     * Get the value from this supplier.  Throws [ConfigException]
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
        override fun get(): ValueType{
            return (source.getterFor(type)(key) as ValueType).also {
                MetaconfigSettings.logger.debug {
                    "ConfigSourceSupplier: key '$key' with value '$it' (type $type) found in source '${source.name}'"
                }
            }
        }

        override fun toString(): String = "ConfigSourceSupplier: key: '$key', type: '$type', source: '${source.name}'"
    }

    /**
     * Converts the type of the result of [origSupplier] from [OriginalType] to
     * [NewType] using the given [converter] function.
     */
    class TypeConvertingSupplier<OriginalType : Any, NewType : Any>(
        private val origSupplier: ConfigValueSupplier<OriginalType>,
        private val converter: (OriginalType) -> NewType
    ) : ConfigValueSupplier<NewType>() {
        override fun get(): NewType {
            val originalValue = origSupplier.get()
            return converter(originalValue).also {
                MetaconfigSettings.logger.debug {
                    "TypeConvertingSupplier: converted retrieved value $originalValue to $it"
                }
            }
        }

        override fun toString(): String = "TypeConvertingSupplier: converting value from $origSupplier"
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
        override fun get(): ValueType {
            val originalValue = origSupplier.get()
            return transformer(originalValue).also {
                MetaconfigSettings.logger.debug {
                    "ValueTransformingSupplier: transformed retrieved value $originalValue to $it"
                }
            }
        }
    }

    class FallbackSupplier<ValueType : Any>(
        private val suppliers: List<ConfigValueSupplier<ValueType>>
    ) : ConfigValueSupplier<ValueType>() {
        override fun get(): ValueType {
            val exceptions = mutableListOf<ConfigException.UnableToRetrieve>()
            for (supplier in suppliers) {
                try {
                    return supplier.get().also {
                        MetaconfigSettings.logger.debug {
                            "FallbackSupplier: value found from supplier $supplier"
                        }
                    }
                } catch (e: ConfigException.UnableToRetrieve) {
                    exceptions += e
                    MetaconfigSettings.logger.debug {
                        "FallbackSupplier: unable to retrieve value from supplier $supplier: $e"
                    }
                }
            }
            throw ConfigException.UnableToRetrieve.NotFound("No suppliers found a value:\n${exceptions.joinToString(separator = "\n")}")
        }
    }
}
