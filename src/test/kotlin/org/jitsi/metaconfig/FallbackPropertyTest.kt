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

class FallbackPropertyTest : ShouldSpec({
    val firstConfigSource = MapConfigSource("legacy config")
    val secondConfigSource = MapConfigSource("new config")

    context("a class with a fallback property") {
        val obj = object {
            val enabled: Boolean by config {
                "old.path.enabled".from(firstConfigSource)
                "server.enabled".from(secondConfigSource)
            }
        }
        context("when the property is present in the first source") {
            firstConfigSource["old.path.enabled"] = true
            should("pull the value from the first config source") {
                obj.enabled shouldBe true
            }
            context("and the second source") {
                secondConfigSource["server.enabled"] = false
                should("still pull the value from the first config source") {
                    obj.enabled shouldBe true
                }
            }
        }
        context("when the property is only present in the second source") {
            secondConfigSource["server.enabled"] = false
            should("pull the value from the second config source") {
                obj.enabled shouldBe false
            }
        }
        context("when the property isn't present in any of the sources") {
            shouldThrow<ConfigException.UnableToRetrieve.NotFound> {
                obj.enabled
            }
        }
    }
})
