package org.jitsi.metaconfig.playground

import org.jitsi.metaconfig.ConfigPropertyNotFoundException
import org.jitsi.metaconfig.ConfigSource
import java.time.Duration
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf


@ExperimentalStdlibApi
private class Foo {
    val interval: Duration by config(
        legacyconfig<Long>("old.path.interval.millis").transformedBy { Duration.ofMillis(it) },
        newconfig("new.path.interval")
    )
}

@ExperimentalStdlibApi
private fun main() {
    val f = Foo()

    println(f.interval)

}

@Suppress("UNCHECKED_CAST")
private sealed class ConfigValueSupplier<ValueType : Any> {
    abstract fun get(): ValueType
    // Reads a key from a config source
    class ConfigSourceSupplier<ValueType : Any>(
        val key: String,
        val source: ConfigSource,
        val type: KType
    ) : ConfigValueSupplier<ValueType>() {
        override fun get(): ValueType {
            return source.getterFor(type)(key) as ValueType
        }
    }

    @Deprecated("didn't work out")
    class RetrievedTypeSupplier<FinalType : Any, RetrievedType : Any>(
        val supplier: ConfigSourceSupplier<FinalType>,
        val retrievedType: KType
    ) : ConfigValueSupplier<RetrievedType>() {
        override fun get(): RetrievedType {
            return supplier.source.getterFor(retrievedType)(supplier.key) as RetrievedType
        }
    }

    class TransformingSupplier<OriginalType : Any, NewType : Any>(
        private val origSupplier: ConfigValueSupplier<OriginalType>,
        private val transformer: (OriginalType) -> NewType
    ) : ConfigValueSupplier<NewType>() {
        override fun get(): NewType {
            return transformer(origSupplier.get())
        }
    }
}

@Suppress("UNCHECKED_CAST")
private class ConfigDelegate<T : Any>(private val suppliers: List<ConfigValueSupplier<T>>) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        for (supplier in suppliers) {
            try {
                return supplier.get()
            } catch (e: ConfigPropertyNotFoundException) {}
        }
        throw ConfigPropertyNotFoundException("we ain't found shit")
    }
}

// Helper to create the delegate based on a set of suppliers
@OptIn(ExperimentalStdlibApi::class)
private inline fun <reified T : Any> config(vararg suppliers: ConfigValueSupplier<T>): ConfigDelegate<T> {
    return ConfigDelegate<T>(suppliers.toList())
}


@ExperimentalStdlibApi
private inline fun <reified T : Any> legacyconfig(keyPath: String): ConfigValueSupplier.ConfigSourceSupplier<T> =
    ConfigValueSupplier.ConfigSourceSupplier(keyPath, legacyConfigSource, typeOf<T>())

@ExperimentalStdlibApi
private inline fun <reified T : Any> newconfig(keyPath: String): ConfigValueSupplier.ConfigSourceSupplier<T> =
    ConfigValueSupplier.ConfigSourceSupplier(keyPath, newConfigSource, typeOf<T>())

// I couldn't get the retrievedAs to work as I want it, it ends up looking like this:
//        legacyconfig<Duration>("old.path.interval.millis").retrievedAs<Duration, Long>().transformedBy { Duration.ofMillis(it) },
// And if I have to pass a type to legacyconfig, I might as well make that the retrieved type and then just do transformedBy
// straight off of that.
@Deprecated("didn't work out")
@ExperimentalStdlibApi
private inline fun <reified FinalType : Any, reified RetrievedType : Any> ConfigValueSupplier.ConfigSourceSupplier<FinalType>.retrievedAs(): ConfigValueSupplier.RetrievedTypeSupplier<FinalType, RetrievedType> {
    return ConfigValueSupplier.RetrievedTypeSupplier<FinalType, RetrievedType>(
        this,
        typeOf<RetrievedType>()
    )
}

@Deprecated("didn't work out")
private fun <OriginalType : Any, NewType : Any> ConfigValueSupplier.RetrievedTypeSupplier<NewType, OriginalType>.transformedBy(transformer: (OriginalType) -> NewType) : ConfigValueSupplier.TransformingSupplier<OriginalType, NewType> {
    return ConfigValueSupplier.TransformingSupplier(this, transformer)
}

private fun <OriginalType : Any, NewType : Any> ConfigValueSupplier<OriginalType>.transformedBy(transformer: (OriginalType) -> NewType) : ConfigValueSupplier.TransformingSupplier<OriginalType, NewType> {
    return ConfigValueSupplier.TransformingSupplier(this, transformer)
}
