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
@ExperimentalStdlibApi
inline fun <reified T : Any> config(configSource: ConfigSource, keyPath: String): ConfigDelegate<T> {
    return ConfigDelegate<T>(ConfigValueSupplier.ConfigSourceSupplier(keyPath, configSource, typeOf<T>()))
}

@ExperimentalStdlibApi
inline fun <reified T : Any> config(supplierBuilder: SupplierBuilderState.Incomplete.KeyAndSource): ConfigDelegate<T> {
    return ConfigDelegate(supplierBuilder.asType<T>().build())
}

@ExperimentalStdlibApi
fun <T : Any> config(vararg supplierBuilders: SupplierBuilderState.Complete<T>): ConfigDelegate<T> {
    return ConfigDelegate(ConfigValueSupplier.FallbackSupplier(supplierBuilders.map { it.build()}))
}

/**
 * Helper for a simple optional property (no fallback)
 *
 * val enabled: Boolean? by optionalConfig(someConfigSource, "path.to.enabled")
 */
@ExperimentalStdlibApi
inline fun <reified T : Any> optionalConfig(configSource: ConfigSource, keyPath: String): OptionalConfigDelegate<T> {
    return OptionalConfigDelegate<T>(ConfigValueSupplier.ConfigSourceSupplier(keyPath, configSource, typeOf<T>()))
}

/**
 * Helper for a simple enum property (no fallback)
 *
 * val color: Color by enumConfig(someConfigSource, "path.to.color")
 */
@ExperimentalStdlibApi
inline fun <reified T : Enum<T>> enumConfig(configSource: ConfigSource, keyPath: String): ConfigDelegate<T> {
    return ConfigDelegate<T>(ConfigValueSupplier.ConfigSourceEnumSupplier(keyPath, configSource, T::class))
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

/**
 * Helper to handle enum types
 */
@ExperimentalStdlibApi
inline fun <reified T : Enum<T>> config(vararg suppliers: ConfigValueSupplier.ConfigSourceEnumSupplier<T>): ConfigDelegate<T> {
    return ConfigDelegate(ConfigValueSupplier.FallbackSupplier(*suppliers))
}

// NOTE(brian): I tried to make this invisible and just use config (above).  'config' _can_ determine
// whether or not the type is nullable via KType and then return a different delegate, but the issue is with
// the return value of 'config': we can't combine 'OptionalConfigDelegate' and 'ConfigDelegate' under a common
// interface since their return types differ (T vs T?). So instead I've added this to separate them.
inline fun <reified T : Any> optionalconfig(vararg suppliers: ConfigValueSupplier<T>): OptionalConfigDelegate<T> {
    return OptionalConfigDelegate(ConfigValueSupplier.FallbackSupplier(*suppliers))
}
