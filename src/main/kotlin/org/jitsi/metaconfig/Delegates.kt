/*
 * Copyright @ 2018 - present 8x8, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jitsi.metaconfig

import org.jitsi.metaconfig.supplier.ConfigValueSupplier
import org.jitsi.metaconfig.supplier.FallbackSupplier
import kotlin.reflect.KProperty
import kotlin.reflect.typeOf

/**
 * A delegate for a configuration property which takes a [ConfigValueSupplier]
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
        } catch (e: ConfigException.UnableToRetrieve.ConditionNotMet) {
            throw e
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
 *
 * throws [ConfigException.UnableToRetrieve] if the property couldn't be retrieved
 */
inline fun <reified T : Any> config(configPropertyState: ConfigPropertyState.Incomplete.KeyAndSource): ConfigDelegate<T> {
    return ConfigDelegate(configPropertyState.asType<T>().build())
}

/**
 * Create a [ConfigDelegate] which will query multiple [ConfigValueSupplier]s, in order, for a
 * given property.
 *
 * throws [ConfigException.UnableToRetrieve] if the property couldn't be retrieved
 */
inline fun <reified T : Any> config(block: SupplierBuilder<T>.() -> Unit): ConfigDelegate<T> {
    val supplier = SupplierBuilder<T>(typeOf<T>()).apply(block)
    with(supplier.getSuppliersNew()) {
        return if (size == 1) {
            // Avoid wrapping in a FallbackSupplier if we don't need one
            ConfigDelegate(first())
        } else {
            return ConfigDelegate(FallbackSupplier(this))
        }
    }
}

/**
 * Create a [ConfigValueSupplier] which can be used for a non-member field, e.g.:
 *
 * fun main(args: Array<String>) {
 *     val port: ConfigValueSupplier<Int> = configSupplier {
 *         retrieve("app.port".from(configSource))
 *     }
 * }
 */
inline fun <reified T : Any> configSupplier(block: SupplierBuilder<T>.() -> Unit): ConfigValueSupplier<T> {
    val supplier = SupplierBuilder<T>(typeOf<T>()).apply(block)
    return FallbackSupplier(supplier.getSuppliersNew())
}

/**
 * Create an [OptionalConfigDelegate] for a single property (no fallback) from only a key and source (filling in
 * the type automatically), returns null if the property couldn't be retrieved
 */
inline fun <reified T : Any> optionalconfig(configPropertyState: ConfigPropertyState.Incomplete.KeyAndSource): OptionalConfigDelegate<T> {
    return OptionalConfigDelegate(configPropertyState.asType<T>().build())
}

/**
 * Create an [OptionalConfigDelegate] which queries multiple [ConfigValueSupplier]s for a property, returning
 * null if the property couldn't be retrieved
 */
inline fun <reified T : Any> optionalconfig(block: SupplierBuilder<T>.() -> Unit): OptionalConfigDelegate<T> {
    val supplier = SupplierBuilder<T>(typeOf<T>()).apply(block)
    with(supplier.getSuppliersNew()) {
        return if (size == 1) {
            // Avoid wrapping in a FallbackSupplier if we don't need one
            OptionalConfigDelegate(first())
        } else {
            return OptionalConfigDelegate(FallbackSupplier(this))
        }
    }
}
