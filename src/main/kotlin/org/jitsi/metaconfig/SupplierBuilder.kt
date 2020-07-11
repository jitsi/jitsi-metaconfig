package org.jitsi.metaconfig

import java.time.Duration
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@ExperimentalStdlibApi
sealed class SupplierBuilderState {
    sealed class Complete<T : Any> : SupplierBuilderState() {
        abstract fun build(): ConfigValueSupplier<T>
        class NoTransformation<T : Any>(
            val key: String,
            val source: ConfigSource,
            val type: KType
        ) : Complete<T>() {
            fun andTransformBy(transformer: (T) -> T): ValueTransformation<T> {
                return ValueTransformation<T>(key, source, type, transformer)
            }

            fun <NewType : Any> andConvertBy(converter: (T) -> NewType): TypeTransformation<T, NewType> {
                return TypeTransformation(key, source, type, converter)
            }

            override fun build(): ConfigValueSupplier<T> {
                return ConfigValueSupplier.ConfigSourceSupplier<T>(key, source, type)
            }
        }
        class ValueTransformation<T : Any>(val key: String, val source: ConfigSource, val type: KType, val transformer: (T) -> T) : Complete<T>() {
            override fun build(): ConfigValueSupplier<T> {
                val sourceSupplier = ConfigValueSupplier.ConfigSourceSupplier<T>(key, source, type)
                return ConfigValueSupplier.ValueTransformingSupplier<T>(sourceSupplier, transformer)
            }
        }
        class TypeTransformation<OriginalType : Any, NewType : Any>(val key: String, val source: ConfigSource, val originalType: KType, val converter: (OriginalType) -> NewType) : Complete<NewType>() {
            override fun build(): ConfigValueSupplier<NewType> {
                val sourceSupplier = ConfigValueSupplier.ConfigSourceSupplier<OriginalType>(key, source, originalType)
                return ConfigValueSupplier.TypeConvertingSupplier<OriginalType, NewType>(sourceSupplier, converter)
            }
        }
    }

    sealed class Incomplete : SupplierBuilderState() {
        object Empty : Incomplete() {
            fun lookup(key: String): KeyOnly = KeyOnly(key)
        }
        class KeyOnly(val key: String) : Incomplete() {
            fun from(configSource: ConfigSource): KeyAndSource {
                return KeyAndSource(key, configSource)
            }
        }
        class KeyAndSource(val key: String, val source: ConfigSource) : Incomplete() {
            inline fun <reified T : Any> asType(): Complete.NoTransformation<T> {
                return Complete.NoTransformation(key, source, typeOf<T>())

            }
        }
    }
}

@ExperimentalStdlibApi
fun lookup(key: String) = SupplierBuilderState.Incomplete.Empty.lookup(key)

val source = MapConfigSource()

fun <T : Any> configs(vararg supplier: ConfigValueSupplier<T>) {

}

@ExperimentalStdlibApi
fun <T : Any>configs2(vararg supplierBuilders: SupplierBuilderState.Complete<T>): ConfigDelegate<T> {
    //TODO: add/change ctor to FallbackSupplier which takes a list instead of forcing into a type array to spread here
    val suppliers = supplierBuilders.map { it.build() }.toTypedArray()
    return ConfigDelegate(ConfigValueSupplier.FallbackSupplier(*suppliers))
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