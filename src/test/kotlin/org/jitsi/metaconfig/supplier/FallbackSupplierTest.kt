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
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jitsi.metaconfig.ConfigException

class FallbackSupplierTest : ShouldSpec({
    var missingCallCount = 0
    val missingSupplier = LambdaSupplier<Int> {
        missingCallCount++
        throw ConfigException.UnableToRetrieve.NotFound("not found")
    }

    var wrongTypeCallCount = 0
    val wrongTypeSupplier = LambdaSupplier<Int> {
        wrongTypeCallCount++
        throw ConfigException.UnableToRetrieve.WrongType("wrong type")
    }

    var workingCallCount = 0
    val workingSupplier = LambdaSupplier {
        workingCallCount++
        42
    }

    context("a fallback supplier") {
        context("where the first inner supplier has the value") {
            val fs = FallbackSupplier(listOf(workingSupplier, missingSupplier, wrongTypeSupplier))
            should("not query the other suppliers") {
                fs.get() shouldBe 42
                workingCallCount shouldBe 1
                missingCallCount shouldBe 0
                wrongTypeCallCount shouldBe 0
            }
        }
        context("where the first inner supplier can't find the value") {
            val fs = FallbackSupplier(listOf(missingSupplier, workingSupplier, wrongTypeSupplier))
            should("still get the correct value") {
                fs.get() shouldBe 42
                missingCallCount shouldBe 1
                workingCallCount shouldBe 1
                wrongTypeCallCount shouldBe 0
            }
        }
        context("where no suppliers have the value") {
            val fs = FallbackSupplier(listOf(missingSupplier, wrongTypeSupplier))
            val ex = shouldThrow<ConfigException.UnableToRetrieve.NotFound> {
                fs.get()
            }
            val msg = ex.message
            msg.shouldNotBeNull()
            // The exception message should contain the inner exception messages
            with(msg) {
                shouldContain("ConfigException\$UnableToRetrieve\$NotFound")
                shouldContain("ConfigException\$UnableToRetrieve\$WrongType")
            }
        }
    }
})
