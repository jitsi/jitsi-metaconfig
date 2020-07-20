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

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.time.Duration

class TypeConvertingPropertyTest : ShouldSpec({
    val configSrc = MapConfigSource("test")

    context("a class with a proeprty whose type is converted") {
        val obj = object {
            val duration: Duration by config {
                retrieve("interval".from(configSrc).asType<Long>().andConvertBy(Duration::ofMillis))
            }
        }
        context("when the property is present in the config") {
            configSrc["interval"] = 5000L
            should("convert it correctly") {
                obj.duration shouldBe Duration.ofSeconds(5)
            }
        }
    }
})
