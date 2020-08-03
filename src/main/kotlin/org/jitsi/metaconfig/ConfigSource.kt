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

import kotlin.reflect.KType

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
     * key, or throw [ConfigException.UnsupportedType] if the type isn't
     * supported.
     */
    fun getterFor(type: KType): (String) -> Any

    /**
     * A name for this [ConfigSource] to give extra context in the
     * event of errors
     */
    val name: String

    /**
     * A description of this [ConfigSource] (this could, for example, include information about the origin of the
     * configuration properties).
     */
    val description: String
        get() = "$name (no description provided)"
}
