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

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.jitsi.disableCachingFor
import org.jitsi.metaconfig.MetaconfigSettings

class CacheTest : ShouldSpec({
    context("a cached property") {
        var callCount = 0
        val obj = object {
            val num: Int by cache { callCount++ }
        }
        context("when the cache is enabled") {
            MetaconfigSettings.cacheEnabled = true
            should("only call the supplier function once") {
                obj.num shouldBe 0
                obj.num shouldBe 0
                obj.num shouldBe 0
                obj.num shouldBe 0
                callCount shouldBe 1
            }
        }
        context("when the cache is disabled") {
            should("call the supplier every time") {
                disableCachingFor {
                    obj.num shouldBe 0
                    obj.num shouldBe 1
                    obj.num shouldBe 2
                    obj.num shouldBe 3
                    callCount shouldBe 4
                }
            }
        }
    }
})
