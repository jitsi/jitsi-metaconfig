package org.jitsi.metaconfig

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.typeOf

/**
 * A delegate for a configuration property which takes a list of [ConfigValueSupplier]s.
 * The suppliers will be queried, in order, for the value and will stop when it is
 * found.  If none of the suppliers find the config property, [ConfigPropertyNotFoundException]
 * is thrown.
 */
open class ConfigDelegate<T : Any>(private val supplier: ConfigValueSupplier<T>) {
    open operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return supplier.get()
    }
}

/**
 * A config property delegate which will return null if the property isn't found
 */
class OptionalConfigDelegate<T : Any>(private val supplier: ConfigValueSupplier<T>) {
    operator fun getValue(thisRef: Any, property: KProperty<*>): T? {
        return try {
            supplier.get()
        } catch (t: Throwable) {
            // TODO: originally I caught ConfigPropertyNotFoundException, but that isn't
            // what's thrown when we query another library.  Is it best to leave this
            // as catching all exceptions?  Ideally there'd be a hook in the config-specific
            // code to translate the exception
//        } catch (e: ConfigPropertyNotFoundException) {
            null
        }
    }
}

// Simple property with no fallback, just creates a delegate directly
/**
 * Helper for a simple property (no fallback)
 *
 * val enabled: Boolean by config(someConfigSource, "path.to.enabled")
 */
//@ExperimentalStdlibApi
//inline fun <reified T : Any> config(configSource: ConfigSource, keyPath: String): ConfigDelegate<T> {
//    return ConfigDelegate<T>(ConfigValueSupplier.ConfigSourceSupplier(keyPath, configSource, typeOf<T>()))
//}

/**
 * Create a [ConfigDelegate] from only a key and source and fill in the type automatically, enables doing:
 *   val port: Int by config("app.server.port".from(configSource))
 * Instead of:
 *   val port: Int by config("app.server.port".from(configSource).asType<Int>())
 * Since the inline function can fill in the type on its own.
 */
@ExperimentalStdlibApi
inline fun <reified T : Any> config(supplierBuilder: SupplierBuilderState.Incomplete.KeyAndSource): ConfigDelegate<T> {
    return ConfigDelegate(supplierBuilder.asType<T>().build())
}

@ExperimentalStdlibApi
inline fun <reified T : Any> config(block: SupplierBuilder<T>.() -> Unit): ConfigDelegate<T> {
    val supplier = SupplierBuilder<T>(typeOf<T>()).apply(block)
    return ConfigDelegate(ConfigValueSupplier.FallbackSupplier(supplier.suppliers.map { it.build()}))
}

@ExperimentalStdlibApi
inline fun <reified T : Any> optionalconfig(block: SupplierBuilder<T>.() -> Unit): OptionalConfigDelegate<T> {
    val supplier = SupplierBuilder<T>(typeOf<T>()).apply(block)
    return OptionalConfigDelegate(ConfigValueSupplier.FallbackSupplier(supplier.suppliers.map { it.build()}))
}

@ExperimentalStdlibApi
inline fun <reified T : Enum<T>> enumconfig(vararg supplierBuilders: SupplierBuilderState.Complete<String>): ConfigDelegate<T> {
    TODO()
//    return ConfigDelegate(ConfigValueSupplier.FallbackSupplier(supplierBuilders.map { it.build() }))
}

/**
 * A helper function to create a [ConfigDelegate] from a variable amount of [ConfigValueSupplier]s
 *
 * val enabled: Boolean by config(
 *      from(configSourceA, "config.path.a"),
 *      from(configSourceB, "config.path.b"),
 * )
 */
@ExperimentalStdlibApi
inline fun <reified T : Any> config(vararg suppliers: ConfigValueSupplier<T>): ConfigDelegate<T> {
    return ConfigDelegate(ConfigValueSupplier.FallbackSupplier(*suppliers))
}
