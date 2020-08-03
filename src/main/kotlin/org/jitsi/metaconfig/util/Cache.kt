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

package org.jitsi.metaconfig.util

import org.jitsi.metaconfig.MetaconfigSettings
import kotlin.reflect.KProperty

/**
 * A delegate which conditionally caches the result of an underlying supplier based on
 * the value of [MetaconfigSettings.cacheEnabled].
 */
class Cache<T : Any>(
    private val supplier: () -> T
) {
    private val cachedValue: ConfigResult<T> by lazy { resultOf(supplier) }

    operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return if (MetaconfigSettings.cacheEnabled) {
            cachedValue.getOrThrow()
        } else {
            supplier()
        }
    }
}

fun <T : Any> cache(supplier: () -> T): Cache<T> = Cache(supplier)
