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

class MetaconfigSettings {
    companion object {
        val DefaultLogger = NoOpLogger

        /**
         * A logger for metaconfig to use, if desired.  Defaults
         * to a no-op implementation
         */
        var logger: MetaconfigLogger = DefaultLogger

        /**
         * A hook to disable caching.  This is only intended to be used for testing
         * and is useful if a config property is defined as static (and therefore
         * only loaded once).  Disabling the cache will cause the property to re-look
         * up the value each time.
         */
        var cacheEnabled: Boolean = true

        /**
         * A hook to force retrieval of the config value immediately upon creation (rather than
         * lazily on first access).  This is only intended to be used for testing and currently
         * only works on ConfigSourceSuppliers.
         */
        var retrieveValuesImmediately: Boolean = false
    }
}

interface MetaconfigLogger {
    fun warn(block: () -> String)
    fun error(block: () -> String)
    fun debug(block: () -> String)
}

val NoOpLogger = object : MetaconfigLogger {
    override fun error(block: () -> String) {}
    override fun warn(block: () -> String) {}
    override fun debug(block: () -> String) {}
}

val StdOutLogger = object : MetaconfigLogger {
    override fun error(block: () -> String) {
        println("ERROR: ${block()}")
    }
    override fun warn(block: () -> String) {
        println("WARN: ${block()}")
    }
    override fun debug(block: () -> String) {
        println("DEBUG: ${block()}")
    }
}
