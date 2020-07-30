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

package org.jitsi.metaconfig.supplier

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jitsi.metaconfig.ConfigException

class LambdaSupplierTest : ShouldSpec({
    var lambdaCallCount = 0
    val lambda = {
        lambdaCallCount++
        42
    }

    context("a LambdaSupplier") {
        val ls = LambdaSupplier(lambda)
        should("get the value from the given lambda") {
            ls.get() shouldBe 42
            lambdaCallCount shouldBe 1
        }
        context("with a context hint") {
            val lsc = LambdaSupplier("hard-coded value of 42", lambda)
            should("include the context in the toString") {
                lsc.toString() shouldContain "hard-coded value of 42"
            }
        }
        context("that throws any kind of exception") {
            val lsc = LambdaSupplier<Int> { throw RuntimeException() }
            should("throw NotFound") {
                shouldThrow<ConfigException.UnableToRetrieve.Error> {
                    lsc.get()
                }
            }
        }
    }
})
