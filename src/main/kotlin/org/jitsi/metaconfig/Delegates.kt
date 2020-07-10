package org.jitsi.metaconfig

import kotlin.reflect.KProperty

/**
 * A delegate for a configuration property which takes a list of [ConfigValueSupplier]s.
 * The suppliers will be queried, in order, for the value and will stop when it is
 * found.  If none of the suppliers find the config property, [ConfigPropertyNotFoundException]
 * is thrown.
 */
class ConfigDelegate<T : Any>(private val supplier: ConfigValueSupplier<T>) {
    operator fun getValue(thisRef: Any, property: KProperty<*>): T {
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
        } catch (e: ConfigPropertyNotFoundException) {
            null
        }
    }
}
/**
 * A helper function to create a [ConfigDelegate] from a variable amount of [ConfigValueSupplier]s
 */
@ExperimentalStdlibApi
inline fun <reified T : Any> config(vararg suppliers: ConfigValueSupplier<T>): ConfigDelegate<T> {
    return ConfigDelegate(ConfigValueSupplier.FallbackSupplier(*suppliers))
}

// NOTE(brian): I tried to make this invisible and just use config (above).  'config' _can_ determine
// whether or not the type is nullable via KType and then return a different delegate, but the issue is with
// the return value of 'config': we can't combine 'OptionalConfigDelegate' and 'ConfigDelegate' under a common
// interface since their return types differ (T vs T?). So instead I've added this to separate them.
inline fun <reified T : Any> optionalconfig(vararg suppliers: ConfigValueSupplier<T>): OptionalConfigDelegate<T> {
    return OptionalConfigDelegate(ConfigValueSupplier.FallbackSupplier(*suppliers))
}
