// ktlint-disable filename

package org.jitsi.metaconfig

import kotlin.reflect.KType

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
        override fun get(): ValueType {
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
