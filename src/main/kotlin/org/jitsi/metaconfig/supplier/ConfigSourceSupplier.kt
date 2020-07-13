package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.ConfigSource
import org.jitsi.metaconfig.MetaconfigSettings
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
) : ConfigValueSupplier<ValueType> {

    @Suppress("UNCHECKED_CAST")
    override fun get(): ValueType {
        return (source.getterFor(type)(key) as ValueType).also {
            MetaconfigSettings.logger.debug {
                "${this::class.simpleName}: key '$key' with value '$it' (type $type) found in source '${source.name}'"
            }
        }
    }

    override fun toString(): String = "${this::class.simpleName}: key: '$key', type: '$type', source: '${source.name}'"
}
