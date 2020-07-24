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
import java.time.Duration
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * This class enables us to implicitly inject the type when a caller is building a property, for example:
 * val bool: Boolean by config {
 *     "some.path".from(configSource) // no need to explicitly set the type
 *     "some.other.path".from(configSource).andTransformBy { !it } // again, don't need to set the type
 *      // can override the inferred type when you want to convert
 *     "some.third.path".from(configSource).asType<Int>().andConvertBy { it > 0 }
 * }
 */
class SupplierBuilder<T : Any>(val finalType: KType) {
    private val suppliers = mutableListOf<ConfigValueSupplier<T>>()

    // We add the states as soon as they're complete to this list since we don't know if another operation
    // will be applied to them or not.  If another operation is performed on a state (via the extension
    // functions defined in the SupplierBuilder scope) we remove the old instance and add the new one.
    // It's assumed that each property's definition will be finished before doing the next one, so
    // there isn't an issue with ordering.  I.e., don't do:
    // {
    //    val x = "some.key".from(source)
    //    "other.key".from(otherSource)
    //    x.andTransformBy { !it }
    // }
    // as this would result in the "other.key" property being checked first
    val propStates = mutableListOf<Any>()

    // TODO: i think we can improve this.  just need a way to fit lambda/onlyif wrappers into
    // the configpropertystate type
    fun getSuppliersNew(): List<ConfigValueSupplier<T>> {
        return propStates.map {
            (it as? ConfigPropertyState.Complete<T>)?.build() ?: it as ConfigValueSupplier<T>
        }
    }

    /**
     * Once the key and source are set, automatically set the inferred type.  If the user wants to retrieve
     * as a different type, they can call 'asType' on their own and override the inferred type.
     */
    fun String.from(configSource: ConfigSource): ConfigPropertyState.Complete.NoTransformation<T> {
        return ConfigPropertyState.Incomplete.Empty.lookup(this).from(configSource).asType<T>(finalType).also {
            propStates += it
        }
    }

    fun ConfigPropertyState.Complete<T>.softDeprecated(msg: String): ConfigPropertyState.Complete<T> {
        propStates -= this
        return withDeprecation(softDeprecation(msg)).also {
            propStates += it
        }
    }

    fun ConfigPropertyState.Complete<T>.hardDeprecated(msg: String): ConfigPropertyState.Complete<T> {
        propStates -= this
        return withDeprecation(hardDeprecation(msg)).also {
            propStates += it
        }
    }

    /**
     * Add a value transformation operation
     */
    fun ConfigPropertyState.Complete.NoTransformation<T>.transformedBy(transformer: (T) -> T): ConfigPropertyState.Complete.ValueTransformation<T> {
        propStates -= this
        return ConfigPropertyState.Complete.ValueTransformation(ConfigSourceSupplier(key, source, type, deprecation), transformer).also {
            propStates += it
        }
    }

    /**
     * Add a type conversion operation
     */
    inline fun <reified RetrieveType : Any> ConfigPropertyState.Complete.NoTransformation<T>.convertFrom(noinline converter: (RetrieveType) -> T): ConfigPropertyState.Complete.TypeConversion<RetrieveType, T> {
        propStates -= this
        return ConfigPropertyState.Complete.TypeConversion(
            ConfigSourceSupplier(key, source, typeOf<RetrieveType>(), deprecation),
            converter
        ).also {
            propStates += it
        }
    }

    /**
     * [LambdaSupplier]s don't require construction as they are entirely responsible for producing
     * the value, so they have their own method
     */
    operator fun String.invoke(lambda: () -> T) {
        propStates += LambdaSupplier(this, lambda)
    }

    fun onlyIf(condition: Condition, block: SupplierBuilder<T>.() -> Unit) {
        val supplier = SupplierBuilder<T>(finalType).apply(block)
        propStates += ConditionalSupplier(condition, supplier.getSuppliersNew())
    }
    fun onlyIf(context: String, predicate: () -> Boolean, block: SupplierBuilder<T>.() -> Unit) =
        onlyIf(Condition(context, predicate), block)
}

/**
 * A standalone 'lookup' function which can be called to 'kick off' the construction of a [ConfigPropertyState]
 *
 * This allows doing:
 *   val port: Int by config("app.server.port".from(configSource))
 * instead of
 *   val port: Int by config(lookup("app.server.port").from(configSource))
 */
fun String.from(configSource: ConfigSource) = ConfigPropertyState.Incomplete.Empty.lookup(this).from(configSource)

fun main() {
    MetaconfigSettings.logger = StdOutLogger
    val src = MapConfigSource("test") {
        put("key2", 42)
        put("key1", Duration.ofSeconds(5))
        put("key3", 10)
    }
    val obj = object {
        val optionA: Int by config {
            "key1".from(src).convertFrom<Duration> { it.toMillis().toInt() }.softDeprecated("don't")
            "key2".from(src).transformedBy { it + 1 }.softDeprecated("don't...")
            "key3".from(src)
            "default" { 42 }
        }
//        val optionB: Int by config {
//            retrieve("key1".from(src).asType<Duration>().andConvertBy { it.toMillis().toInt() })
//            retrieve("key2".from(src).andTransformBy { it + 1 })
//            retrieve("key3".from(src))
//        }
    }
    println(obj.optionA)
}
