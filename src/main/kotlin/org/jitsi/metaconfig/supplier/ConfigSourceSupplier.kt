package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.ConfigSource
import org.jitsi.metaconfig.Deprecation
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
    private val type: KType,
    deprecation: Deprecation = Deprecation.NotDeprecated
) : ConfigValueSupplier<ValueType>(deprecation) {

    @Suppress("UNCHECKED_CAST")
    override fun doGet(): ValueType = (source.getterFor(type)(key) as ValueType)

    override fun toString(): String = "${this::class.simpleName}: key: '$key', type: '$type', source: '${source.name}'"
}
