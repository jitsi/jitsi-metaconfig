package org.jitsi.metaconfig.supplier

import org.jitsi.metaconfig.ConfigException
import org.jitsi.metaconfig.MetaconfigSettings

class LambdaSupplier<ValueType : Any>(
    private val context: String,
    private val supplier: () -> ValueType
) : ConfigValueSupplier<ValueType>() {
    constructor(supplier: () -> ValueType) : this("", supplier)

    override fun doGet(): ValueType {
        MetaconfigSettings.logger.debug {
            "${this::class.simpleName}: Trying to retrieve value via $context"
        }
        return try {
            supplier().also {
                MetaconfigSettings.logger.debug {
                    "$this: found value"
                }
            }
        } catch (e: ConfigException) {
            throw e
        } catch (t: Throwable) {
            throw ConfigException.UnableToRetrieve.NotFound("Lambda supplier $context unable to retrieve value")
        }
    }

    override fun toString(): String = "${this::class.simpleName}${if (context.isNotBlank()) ": '$context'" else ""}"
}
