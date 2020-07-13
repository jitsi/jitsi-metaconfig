package org.jitsi.metaconfig

import org.jitsi.metaconfig.supplier.ConfigSourceSupplier
import org.jitsi.metaconfig.supplier.ConfigValueSupplier
import org.jitsi.metaconfig.supplier.TypeConvertingSupplier
import org.jitsi.metaconfig.supplier.ValueTransformingSupplier
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * State classes to aid in the construction of a [ConfigValueSupplier].
 *
 * These classes allow constructing the values needed to build a [ConfigValueSupplier] piece-by-piece, and only
 * allow the creation of a [ConfigValueSupplier] when all the required pieces are present.  Optional operations
 * can be added as well (for doing value or type conversions).
 *
 * Note that currently the construction must be performed in a specific
 * order: (key, source, type [,valueTransformation|typeTransformation]).  And only one transformation
 * (value or type) is allowed.  All of this could be changed by adding the more methods to the different states.
 */
@ExperimentalStdlibApi
sealed class SupplierBuilderState {
    sealed class Complete<T : Any> : SupplierBuilderState() {
        /**
         * Build a [ConfigValueSupplier].  Required for any subclass of [Complete].
         */
        abstract fun build(): ConfigValueSupplier<T>

        /**
         * A simple lookup of a property value from the given source as the given type
         */
        class NoTransformation<T : Any>(val key: String, val source: ConfigSource, val type: KType) : Complete<T>() {
            /**
             * Add a value transformation operation
             */
            fun andTransformBy(transformer: (T) -> T): ValueTransformation<T> {
                return ValueTransformation<T>(key, source, type, transformer)
            }

            /**
             * Add a type conversion operation
             */
            fun <NewType : Any> andConvertBy(converter: (T) -> NewType): TypeConversion<T, NewType> {
                return TypeConversion(key, source, type, converter)
            }

            /**
             * We allow updating the type here so that helper functions can fill in the type
             * automatically (since they can derive it from the call) so the caller doesn't have
             * to, however, the helpers will use the final type as a guess; if the caller wants
             * to retrieve the property as another type and then convert it, we need to let them
             * set the retrieved type via this asType call and then use [andConvertBy].  For example:
             * val bool: Boolean by config {
             *     // No need to use 'asType<Boolean>', we can determine that
             *     "app.enabled".from(newConfigSource)
             *     // Here the caller didn't want to retrieve it as bool, so they override the type
             *     // via another call to 'asType' and then convert the value to a Boolean.
             *     "app.enabled".from(newConfigSource).asType<Int>.andConvertBy { it > 0 }
             * }
             */
            inline fun <reified R : Any> asType(): NoTransformation<R> {
                return NoTransformation(key, source, typeOf<R>())
            }

            override fun build(): ConfigValueSupplier<T> {
                return ConfigSourceSupplier<T>(key, source, type)
            }
        }

        /**
         * A lookup which transforms the value in some way
         */
        class ValueTransformation<T : Any>(
            val key: String,
            val source: ConfigSource,
            val type: KType,
            val transformer: (T) -> T
        ) : Complete<T>() {
            override fun build(): ConfigValueSupplier<T> {
                val sourceSupplier = ConfigSourceSupplier<T>(key, source, type)
                return ValueTransformingSupplier<T>(sourceSupplier, transformer)
            }
        }

        /**
         * A lookup which converts the type the value was retrieved as to another type
         */
        class TypeConversion<OriginalType : Any, NewType : Any>(
            val key: String,
            val source: ConfigSource,
            val originalType: KType,
            val converter: (OriginalType) -> NewType
        ) : Complete<NewType>() {
            override fun build(): ConfigValueSupplier<NewType> {
                val sourceSupplier = ConfigSourceSupplier<OriginalType>(key, source, originalType)
                return TypeConvertingSupplier(sourceSupplier, converter)
            }
        }
    }

    sealed class Incomplete : SupplierBuilderState() {
        /**
         * The initial empty state
         */
        object Empty : Incomplete() {
            fun lookup(key: String): KeyOnly = KeyOnly(key)
        }

        /**
         * Only the config key is present
         */
        class KeyOnly(val key: String) : Incomplete() {
            fun from(configSource: ConfigSource): KeyAndSource {
                return KeyAndSource(key, configSource)
            }
        }

        /**
         * The config key and source are present
         */
        class KeyAndSource(val key: String, val source: ConfigSource) : Incomplete() {
            inline fun <reified T : Any> asType(): Complete.NoTransformation<T> {
                return Complete.NoTransformation(key, source, typeOf<T>())
            }
            fun <T : Any> asType(type: KType): Complete.NoTransformation<T> {
                return Complete.NoTransformation(key, source, type)
            }
        }
    }
}

/**
 * This class enables us to implicitly inject the type when a caller is building a property, for example:
 * val bool: Boolean by config {
 *     "some.path".from(configSource) // no need to explicitly set the type
 *     "some.other.path".from(configSource).andTransformBy { !it } // again, don't need to set the type
 *      // can override the inferred type when you want to convert
 *     "some.third.path".frorm(configSource).asType<Int>().andConvertBy { it > 0 }
 * }
 */
@ExperimentalStdlibApi
class SupplierBuilder<T : Any>(val finalType: KType) {
    val suppliers = mutableListOf<SupplierBuilderState.Complete<T>>()

    fun retrieve(sbs: SupplierBuilderState.Complete<T>) {
        suppliers += sbs
    }

    /**
     * Once the key and source are set, automatially set the inferred type.  If the user wants to retrieve
     * as a different type, they can call 'asType' on their own and override the inferred type.
     */
    fun String.from(configSource: ConfigSource) =
        SupplierBuilderState.Incomplete.Empty.lookup(this).from(configSource).asType<T>(finalType)
}

/**
 * A standalone 'lookup' function which can be called to 'kick off' the construction of a ConfigValueSupplier
 *
 * This allows doing:
 *   val port: Int by config("app.server.port".from(configSource))
 * instead of
 *   val port: Int by config(lookup("app.server.port").from(configSource))
 */
@ExperimentalStdlibApi
fun String.from(configSource: ConfigSource) = SupplierBuilderState.Incomplete.Empty.lookup(this).from(configSource)
