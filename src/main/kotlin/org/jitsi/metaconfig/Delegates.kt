package org.jitsi.metaconfig

import kotlin.reflect.KProperty
import kotlin.reflect.typeOf

/**
 * A delegate for a configuration property which takes a list of [ConfigValueSupplier]s.
 * The suppliers will be queried, in order, for the value and will stop when it is
 * found.  If none of the suppliers find the config property, [ConfigException.NotFound]
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
        } catch (t: ConfigException.UnableToRetrieve) {
            null
        }
    }
}

/**
 * Create a [ConfigDelegate] for a single property (no fallback) from only a key and source and fill in
 * the type automatically, enables doing:
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
    return ConfigDelegate(ConfigValueSupplier.FallbackSupplier(supplier.suppliers.map { it.build() }))
}

@ExperimentalStdlibApi
inline fun <reified T : Any> optionalconfig(supplierBuilder: SupplierBuilderState.Incomplete.KeyAndSource): OptionalConfigDelegate<T> {
    return OptionalConfigDelegate(supplierBuilder.asType<T>().build())
}
@ExperimentalStdlibApi
inline fun <reified T : Any> optionalconfig(block: SupplierBuilder<T>.() -> Unit): OptionalConfigDelegate<T> {
    val supplier = SupplierBuilder<T>(typeOf<T>()).apply(block)
    return OptionalConfigDelegate(ConfigValueSupplier.FallbackSupplier(supplier.suppliers.map { it.build() }))
}
