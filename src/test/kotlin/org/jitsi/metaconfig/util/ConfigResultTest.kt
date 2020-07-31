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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ConfigResultTest : ShouldSpec({
    context("resultOf") {
        should("return success if the block succeeds") {
            resultOf { 42 }.shouldBeInstanceOf<ConfigResult.Success<Int>>()
        }
        should("return failure if the block throws") {
            resultOf { throw NullPointerException() }.shouldBeInstanceOf<ConfigResult.Failure>()
        }
    }
    context("getOrThrow") {
        should("return the value if the block succeeds") {
            resultOf { 42 }.getOrThrow() shouldBe 42
        }
        should("throw if the block throws") {
            shouldThrow<NullPointerException> {
                resultOf<Int> { throw NullPointerException() }.getOrThrow()
            }
        }
    }
})
