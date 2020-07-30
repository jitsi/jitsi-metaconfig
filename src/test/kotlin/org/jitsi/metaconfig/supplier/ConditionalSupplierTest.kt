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
import org.jitsi.metaconfig.Condition
import org.jitsi.metaconfig.ConfigException

class ConditionalSupplierTest : ShouldSpec({
    context("a conditional supplier") {
        context("when the condition is met") {
            val cs = ConditionalSupplier<Int>(
                Condition("enabled", { true }),
                listOf(
                    LambdaSupplier { 42 }
                )
            )
            should("return the value") {
                cs.get() shouldBe 42
            }
        }
        context("when the condition is not met") {
            val cs = ConditionalSupplier<Int>(
                Condition("enabled", { false }),
                listOf(
                    LambdaSupplier { 42 }
                )
            )
            should("throw an exception") {
                shouldThrow<ConfigException.UnableToRetrieve.ConditionNotMet> {
                    cs.get()
                }
            }
        }
        context("when the condition is met and the inner supplier throws") {
            val cs = ConditionalSupplier<Int>(
                Condition("enabled", { true }),
                listOf(
                    LambdaSupplier<Int> { throw NullPointerException() }
                )
            )
            should("translate the exception") {
                shouldThrow<ConfigException.UnableToRetrieve> {
                    cs.get()
                }
            }
        }
    }
})
