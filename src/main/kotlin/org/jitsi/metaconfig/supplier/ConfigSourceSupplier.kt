package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.ConfigException
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
    private val deprecation: Deprecation
) : ConfigValueSupplier<ValueType>() {
    private var deprecationWarningLogged = false

    @Suppress("UNCHECKED_CAST")
    override fun doGet(): ValueType {
        MetaconfigSettings.logger.debug {
            "${this::class.simpleName}: Trying to retrieve key '$key' from source '${source.name}' as type $type"
        }
        return (source.getterFor(type)(key) as ValueType).also {
            MetaconfigSettings.logger.debug {
                "${this::class.simpleName}: Successfully retrieved key '$key' from source '${source.name}' as type $type"
            }
            if (deprecation is Deprecation.Deprecated.Soft && !deprecationWarningLogged) {
                MetaconfigSettings.logger.warn {
                    "Key '$key' from source '${source.name}' is deprecated: ${deprecation.msg}"
                }
                deprecationWarningLogged = true
            } else if (deprecation is Deprecation.Deprecated.Hard) {
                throw ConfigException.UnableToRetrieve.Deprecated(
                    "Key '$key' from source '${source.name}' is deprecated: ${deprecation.msg}"
                )
            }
        }
    }

    override fun withDeprecation(deprecation: Deprecation): ConfigValueSupplier<ValueType> =
        ConfigSourceSupplier(key, source, type, deprecation)

    override fun toString(): String = "${this::class.simpleName}: key: '$key', type: '$type', source: '${source.name}'"
}
