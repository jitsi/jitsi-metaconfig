package org.jitsi.metaconfig

import java.time.Duration
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

            override fun build(): ConfigValueSupplier<T> {
                return ConfigValueSupplier.ConfigSourceSupplier<T>(key, source, type)
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
                val sourceSupplier = ConfigValueSupplier.ConfigSourceSupplier<T>(key, source, type)
                return ConfigValueSupplier.ValueTransformingSupplier<T>(sourceSupplier, transformer)
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
                val sourceSupplier = ConfigValueSupplier.ConfigSourceSupplier<OriginalType>(key, source, originalType)
                return ConfigValueSupplier.TypeConvertingSupplier<OriginalType, NewType>(sourceSupplier, converter)
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
        }
    }
}

/**
 * A standalone 'lookup' function which can be called to 'kick off' the construction of a ConfigValueSupplier
 *
 * // TODO: still needed after the string one below?
 */
@ExperimentalStdlibApi
fun lookup(key: String) = SupplierBuilderState.Incomplete.Empty.lookup(key)

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

val source = MapConfigSource()

fun <T : Any> configs(vararg supplier: ConfigValueSupplier<T>) {

}

@ExperimentalStdlibApi
fun <T : Any>configs2(vararg supplierBuilders: SupplierBuilderState.Complete<T>): ConfigDelegate<T> {
    val suppliers = supplierBuilders.map { it.build() }
    return ConfigDelegate(ConfigValueSupplier.FallbackSupplier(suppliers))
}

@ExperimentalStdlibApi
fun bar() {
    configs(
        lookup("some.key").from(source).asType<Long>().build(),
        lookup("some.key").from(source).asType<Duration>().andConvertBy { it.toMillis() }.build()
    )
    configs2(
        lookup("some.key").from(source).asType<Long>(),
        lookup("some.key").from(source).asType<Duration>().andConvertBy { it.toMillis() }
    )
    val z = lookup("some.key")
    val x = lookup("some.key").from(source).asType<Long>().andTransformBy { it * 2 }.build()
    val y = lookup("some.key").from(source).asType<Long>().andConvertBy { Duration.ofMillis(it) }.build()
}