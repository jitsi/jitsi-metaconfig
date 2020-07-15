package org.jitsi.metaconfig

/**
 * Throw when a value for the property couldn't be retrieved [ConfigSource]
 */
sealed class ConfigException(msg: String) : Exception(msg) {
    sealed class UnableToRetrieve(msg: String) : ConfigException(msg) {
        /**
         * The property could not be found
         */
        class NotFound(msg: String) : UnableToRetrieve(msg)

        /**
         * The property was found, but it had a type incompatible with what
         * was requested
         */
        class WrongType(msg: String) : UnableToRetrieve(msg)

        class Deprecated(msg: String) : UnableToRetrieve(msg)
    }

    /**
     * A value was requested as a type which is not supported by
     * the [ConfigSource]
     */
    class UnsupportedType(msg: String) : ConfigException(msg)
}
