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
import org.jitsi.metaconfig.ConfigException

class TypeConvertingSupplierTest : ShouldSpec({
    val workingOrigSupplier = LambdaSupplier { 42 }

    val missingOrigSupplier = LambdaSupplier<Int> {
        throw ConfigException.UnableToRetrieve.NotFound("not found")
    }

    context("a TypeConvertingSupplier") {
        context("with an inner supplier which finds the value") {
            val tcs = TypeConvertingSupplier(workingOrigSupplier) { it > 0 }
            should("convert the type correctly") {
                tcs.get() shouldBe true
            }
        }
        context("with an inner supplier which doesn't find the value") {
            val tcs = TypeConvertingSupplier(missingOrigSupplier) { it > 0 }
            shouldThrow<ConfigException.UnableToRetrieve.NotFound> {
                tcs.get()
            }
        }
        context("whose conversion fails") {
            val tcs = TypeConvertingSupplier<Int, Boolean>(workingOrigSupplier) { throw NullPointerException() }
            should("translate the exception") {
                shouldThrow<ConfigException.UnableToRetrieve.Error> {
                    tcs.get()
                }
            }
        }
    }
})
