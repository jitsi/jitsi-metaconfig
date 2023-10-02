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

@file:Suppress("MemberVisibilityCanBePrivate")

package org.jitsi.metaconfig

import org.jitsi.metaconfig.supplier.ConditionalSupplier
import org.jitsi.metaconfig.supplier.ConfigSourceSupplier
import org.jitsi.metaconfig.supplier.ConfigValueSupplier
import org.jitsi.metaconfig.supplier.LambdaSupplier
import org.jitsi.metaconfig.supplier.TypeConvertingSupplier
import org.jitsi.metaconfig.supplier.ValueTransformingSupplier
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * This class enables us to implicitly inject the type when a caller is building a property, for example:
 * val bool: Boolean by config {
 *     "some.path".from(configSource) // no need to explicitly set the type
 *     "some.other.path".from(configSource).transformedBy { !it } // again, don't need to set the type
 *      // can override the inferred type when you want to convert
 *     "some.third.path".from(configSource).convertFrom<Long> { it > 0 }
 * }
 */
class SupplierBuilder<T : Any>(val finalType: KType) {
    val suppliers = mutableListOf<ConfigValueSupplier<T>>()

    /**
     * Given a key and a source, create a [ConfigSourceSupplier] with an inferred type.
     */
    fun String.from(configSource: ConfigSource): ConfigSourceSupplier<T> {
        return ConfigSourceSupplier<T>(this, configSource, finalType, noDeprecation()).also {
            suppliers += it
        }
    }

    /**
     * Mark the source key of the value from this supplier as soft deprecated
     */
    fun ConfigValueSupplier<T>.softDeprecated(msg: String): ConfigValueSupplier<T> {
        suppliers -= this
        return this.withDeprecation(softDeprecation(msg)).also {
            suppliers += it
        }
    }

    /**
     * Mark the source key of the value from this supplier as hard deprecated
     */
    fun ConfigValueSupplier<T>.hardDeprecated(msg: String): ConfigValueSupplier<T> {
        suppliers -= this
        return this.withDeprecation(hardDeprecation(msg)).also {
            suppliers += it
        }
    }

    /**
     * Add a value transformation operation
     */
    fun ConfigValueSupplier<T>.transformedBy(transformer: (T) -> T): ConfigValueSupplier<T> {
        suppliers -= this
        return ValueTransformingSupplier(this, transformer).also {
            suppliers += it
        }
    }

    /**
     * Add a type conversion operation.  This is only able to be applied on top of a [ConfigSourceSupplier], because
     * we need to recreate the underlying supplier to retrieve a different type than was originally inferred (we can
     * add support for other suppliers where this makes sense as the need arises).
     */
    inline fun <reified RetrieveType : Any> ConfigSourceSupplier<T>.convertFrom(
        noinline converter: (RetrieveType) -> T
    ): TypeConvertingSupplier<RetrieveType, T> {
        suppliers -= this
        return TypeConvertingSupplier(
            // Re-create the underyling ConfigSourceSupplier, but have it retrieve a different type
            this.withRetrievedType(typeOf<RetrieveType>()),
            converter
        ).also {
            suppliers += it
        }
    }

    /**
     * Create a [LambdaSupplier] with a String context, a la:
     * ...by config {
     *    ...
     *    "From an object" { MyFoo.port }
     *    ...
     * }
     * [LambdaSupplier]s don't require construction as they are entirely responsible for producing
     * the value, so they have their own method
     */
    operator fun String.invoke(lambda: () -> T) {
        suppliers += LambdaSupplier(this, lambda)
    }

    /**
     * Wrap a set of inner suppliers in a condition guard
     */
    fun onlyIf(context: String, predicate: () -> Boolean, block: SupplierBuilder<T>.() -> Unit) {
        val supplier = SupplierBuilder<T>(finalType).apply(block)
        suppliers += ConditionalSupplier(Condition(context, predicate), supplier.suppliers)
    }
}

/**
 * A standalone function which can be called to 'kick off' the construction of a [ConfigSourceSupplier] from
 * a key and a [ConfigSource], a la:
 *
 * val port: Int by config("app.server.port".from(configSource))
 */
inline fun <reified T : Any> String.from(configSource: ConfigSource) =
    ConfigSourceSupplier<T>(this, configSource, typeOf<T>(), noDeprecation())
