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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class TransformingPropertyTest : ShouldSpec({
    val configSrc = MapConfigSource("test")

    context("a class with a property whose value is transformed") {
        val obj = object {
            val enabled: Boolean by config {
                "disabled".from(configSrc).transformedBy { !it }
            }
        }
        context("when the property is present in the config source") {
            configSrc["disabled"] = true
            should("transform it correctly") {
                obj.enabled shouldBe false
            }
        }
        context("when the property isn't present in the config source") {
            shouldThrow<ConfigException.UnableToRetrieve.NotFound> {
                obj.enabled
            }
        }
    }
})
