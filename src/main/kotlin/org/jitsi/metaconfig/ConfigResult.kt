package org.jitsi.metaconfig

import org.jitsi.metaconfig.util.hash
import java.util.*
import kotlin.reflect.KClass

/**
 * A [ConfigResult] represents the result of a 'search' for a configuration
 * property's value.  If the property was found and the value parsed
 * successfully as type [T], use [ConfigResult.found] to indicate a found
 * result.  Otherwise [ConfigResult.notFound] can be used to hold the
 * exception.
 */
sealed class ConfigResult<T : Any> {
    /**
     * Models a property whose value was found.  Both the value and the [ConfigSource]
     * from which the value was retrieved are held.
     */
    class PropertyFound<T : Any>(val source: ConfigSource, val value: T) : ConfigResult<T>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as PropertyFound<*>
            return hashCode() == other.hashCode()
        }
        override fun hashCode(): Int = hash(source, value)
        override fun toString(): String = "Found($value)"
    }

    /**
     * Models a property whose value was not found.  The exception
     * encountered when searching is held.
     */
    class PropertyNotFound<T : Any>(val exception: Throwable) : ConfigResult<T>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as PropertyNotFound<*>
            return hashCode() == other.hashCode()
        }
        override fun hashCode(): Int = exception.hashCode()
        override fun toString(): String = "NotFound($exception)"
    }

    companion object {
        fun <T : Any> found(source: ConfigSource, value: T): ConfigResult<T> = PropertyFound(source, value)
        fun <T : Any> notFound(exception: Throwable): ConfigResult<T> = PropertyNotFound(exception)
    }
}

/**
 * If this [ConfigResult] is a [ConfigResult.PropertyFound], return
 * the contained value.  Else throw the contained exception.
 */
fun <T : Any> ConfigResult<T>.getOrThrow(): T {
    return when (this) {
        is ConfigResult.PropertyNotFound -> throw this.exception
        is ConfigResult.PropertyFound -> this.value
    }
}

/**
 * Return true if a value for this property was found,
 * false otherwise.
 */
fun <T : Any> ConfigResult<T>.isFound(): Boolean =
    this is ConfigResult.PropertyFound

