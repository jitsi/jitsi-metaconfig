package org.jitsi.metaconfig

import kotlin.reflect.KProperty

/**
 * A delegate for a configuration property which takes a list of [ConfigValueSupplier]s.
 * The suppliers will be queried, in order, for the value and will stop when it is
 * found.  If none of the suppliers find the config property, [ConfigPropertyNotFoundException]
 * is thrown.
 */
class ConfigDelegate<T : Any>(private val supplier: ConfigValueSupplier<T>) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return supplier.get()
    }
}
/**
 * A helper function to create a [ConfigDelegate] from a variable amount of [ConfigValueSupplier]s
 */
inline fun <reified T : Any> config(vararg suppliers: ConfigValueSupplier<T>): ConfigDelegate<T> {
    return ConfigDelegate(ConfigValueSupplier.FallbackSupplier(*suppliers))
}
