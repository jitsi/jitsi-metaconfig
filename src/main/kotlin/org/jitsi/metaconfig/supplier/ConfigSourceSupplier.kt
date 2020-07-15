package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.ConfigSource
import org.jitsi.metaconfig.Deprecation
import kotlin.reflect.KType

/**
 * Retrieve the given [key] from [source] as [type], where [type] and [ValueType] must 'match'
 */
class ConfigSourceSupplier<ValueType : Any>(
    private val key: String,
    private val source: ConfigSource,
    private val type: KType,
    deprecation: Deprecation
) : ConfigValueSupplier<ValueType>(deprecation) {

    @Suppress("UNCHECKED_CAST")
    override fun doGet(): ValueType = (source.getterFor(type)(key) as ValueType)

    override fun toString(): String = "${this::class.simpleName}: key: '$key', type: '$type', source: '${source.name}'"
}
