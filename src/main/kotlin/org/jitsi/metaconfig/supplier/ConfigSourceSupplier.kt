package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.ConfigSource
import kotlin.reflect.KType

/**
 * Retrieve the given [key] from [source] as [type]
 * ([type] and [ValueType] must 'match')
 *
 *
 * NOTE that this class (and its subclasses) exist primarily to provide extra context
 * and debugging information (otherwise lambda could just be used).
 */
class ConfigSourceSupplier<ValueType : Any>(
    private val key: String,
    private val source: ConfigSource,
    private val type: KType
) : ConfigValueSupplier<ValueType>() {

    @Suppress("UNCHECKED_CAST")
    override fun doGet(): ValueType = (source.getterFor(type)(key) as ValueType)

    override fun toString(): String = "${this::class.simpleName}: key: '$key', type: '$type', source: '${source.name}'"
}
