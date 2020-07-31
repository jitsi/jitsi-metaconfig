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

/**
 * A simplified version of kotlin's [Result], which has issues when returned from
 * a lambda (https://youtrack.jetbrains.com/issue/KT-27586).
 */
sealed class ConfigResult<out T> {
    class Success<out T>(val value: T) : ConfigResult<T>()
    class Failure(val exception: Throwable) : ConfigResult<Nothing>()
}

fun <T> ConfigResult<T>.getOrThrow(): T = when (this) {
    is ConfigResult.Success<T> -> value
    is ConfigResult.Failure -> throw exception
}

inline fun <T> resultOf(block: () -> T): ConfigResult<T> {
    return try {
        ConfigResult.Success(block())
    } catch (t: Throwable) {
        ConfigResult.Failure(t)
    }
}
