@file:Suppress("MemberVisibilityCanBePrivate")

package org.jitsi.metaconfig

import org.jitsi.metaconfig.supplier.ConditionalSupplier
import org.jitsi.metaconfig.supplier.ConfigValueSupplier
import org.jitsi.metaconfig.supplier.LambdaSupplier
import kotlin.reflect.KType

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
    val suppliers = mutableListOf<ConfigValueSupplier<T>>()

    fun retrieve(sbs: ConfigPropertyState.Complete<T>) {
        suppliers += sbs.build()
    }

    /**
     * [LambdaSupplier]s don't require construction as they are entirely responsible for producing
     * the value, so they have their own method
     */
    fun retrieve(context: String, lambda: () -> T) {
        suppliers += LambdaSupplier(context, lambda)
    }

    fun onlyIf(condition: Condition, block: SupplierBuilder<T>.() -> Unit) {
        val supplier = SupplierBuilder<T>(finalType).apply(block)
        suppliers += ConditionalSupplier(condition, supplier.suppliers)
    }
    fun onlyIf(context: String, predicate: () -> Boolean, block: SupplierBuilder<T>.() -> Unit) =
        onlyIf(Condition(context, predicate), block)

    /**
     * Once the key and source are set, automatically set the inferred type.  If the user wants to retrieve
     * as a different type, they can call 'asType' on their own and override the inferred type.
     */
    fun String.from(configSource: ConfigSource) =
        ConfigPropertyState.Incomplete.Empty.lookup(this).from(configSource).asType<T>(finalType)
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
