package org.jitsi.metaconfig

import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A [ConfigSource] is what is used to retrieve configuration values
 * from some location.
 */
interface ConfigSource {
    /**
     * Given a [type], return a function which takes in a
     * configuration property key (aka a key 'name') and returns the value
     * of the property at the given name as the type referred to by [type].
     *
     * The return getter should return the value corresponding to the given
     * key, or throw [ConfigPropertyNotFoundException].
     */
    fun getterFor(type: KType): (String) -> Any

    /**
     * Enums require a special function so that we can get a [T] with
     * proper type boundaries.
     */
    fun <T : Enum<T>> getterFor(enumClazz: Class<T>): (String) -> T

    /**
     * A name for this [ConfigSource] to give extra context in the
     * event of errors
     */
    val name: String
}

class ConfigPropertyNotFoundException(msg: String) : Exception(msg)
