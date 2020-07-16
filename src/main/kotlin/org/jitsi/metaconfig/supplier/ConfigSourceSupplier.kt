package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.ConfigSource
import org.jitsi.metaconfig.Deprecation
import org.jitsi.metaconfig.MetaconfigSettings
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
    override fun doGet(): ValueType {
        MetaconfigSettings.logger.debug {
            "${this::class.simpleName}: Trying to retrieve key '$key' from source '${source.name}' as type $type"
        }
        return (source.getterFor(type)(key) as ValueType).also {
            MetaconfigSettings.logger.debug {
                "${this::class.simpleName}: Successfully retrieved key '$key' from source '${source.name}' as type $type"
            }
        }
    }

    override fun toString(): String = "${this::class.simpleName}: key: '$key', type: '$type', source: '${source.name}'"
}
