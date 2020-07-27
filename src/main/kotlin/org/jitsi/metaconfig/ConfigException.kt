/*
 * Copyright @ 2018 - present 8x8, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jitsi.metaconfig

/**
 * Throw when a value for the property couldn't be retrieved from a [ConfigSource]
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

        class ConditionNotMet(msg: String) : UnableToRetrieve(msg)
    }

    /**
     * A value was requested as a type which is not supported by
     * the [ConfigSource]
     */
    class UnsupportedType(msg: String) : ConfigException(msg)
}
