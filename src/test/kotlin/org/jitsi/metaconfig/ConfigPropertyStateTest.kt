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
import java.time.Duration

class ConfigPropertyStateTest : ShouldSpec({
    val legacyConfig = MapConfigSource("legacy config") {
        put("legacy.num", 42)
        put("legacy.interval", 5000L)
    }
    val newConfig = MapConfigSource("new config") {
        put("new.num", 43)
        put("new.interval", Duration.ofSeconds(7))
    }
    context("property state builders") {
        should("allow defining a property with only a key and a source") {
            val obj = object {
                val num: Int by config("new.num".from(newConfig))
            }
            obj.num shouldBe 43
        }
        should("allow marking a property with a key and a source as soft deprecated") {
            val obj = object {
                val num: Int by config("legacy.num".from(legacyConfig).softDeprecated("use new.num"))
            }
            obj.num shouldBe 42
        }
        should("allow marking a property with a key and a source as hard deprecated") {
            val obj = object {
                val num: Int by config("legacy.num".from(legacyConfig).hardDeprecated("use new.num"))
            }
            shouldThrow<ConfigException.UnableToRetrieve.Deprecated> {
                obj.num
            }
        }
        should("allow marking a property with a key, a source and a transformation as hard deprecated") {
            val obj = object {
                val num: Int by config {
                    retrieve("legacy.num".from(legacyConfig).andTransformBy { it + 1 }.hardDeprecated("use new.num"))
                }
            }
            shouldThrow<ConfigException.UnableToRetrieve.Deprecated> {
                obj.num
            }
        }
        should("allow transforming the value of a property") {
            val obj = object {
                val num: Int by config {
                    retrieve("legacy.num".from(legacyConfig).andTransformBy { it / 2 })
                }
            }
            obj.num shouldBe 21
        }
        should("allow converting the type of a property") {
            val obj = object {
                val interval: Duration by config {
                    retrieve("legacy.interval".from(legacyConfig).asType<Long>().andConvertBy(Duration::ofMillis))
                }
            }
            obj.interval shouldBe Duration.ofMillis(5000)
        }
        should("allow falling back across multiple properties") {
            val obj = object {
                val num: Int by config {
                    retrieve("some.missing.path".from(legacyConfig))
                    retrieve("new.num".from(newConfig))
                }
            }
            obj.num shouldBe 43
        }
        should("allow conditionally enabling a property") {
            val obj = object {
                val enabledNum: Int by config {
                    onlyIf("enabled", { true }) {
                        retrieve("new.num".from(newConfig))
                    }
                }
                val disabledNum: Int by config {
                    onlyIf("enabled", { false} ) {
                        retrieve("new.num".from(newConfig))
                    }
                }
            }
            obj.enabledNum shouldBe 43
            shouldThrow<ConfigException.UnableToRetrieve.ConditionNotMet> {
                obj.disabledNum
            }
        }
        should("throw if an optional, conditional property is disabled") {
            val obj = object {
                val num: Int? by optionalconfig {
                    onlyIf("enabled", { false }) {
                        retrieve("new.num".from(newConfig))
                    }
                }
            }
            shouldThrow<ConfigException.UnableToRetrieve.ConditionNotMet> {
                obj.num
            }
        }
        should("return null if an optional, conditional property is enabled but not found") {
            val obj = object {
                val num: Int? by optionalconfig {
                    onlyIf("enabled", { true }) {
                        retrieve("missing.num".from(newConfig))
                    }
                }
            }
            obj.num shouldBe null
        }
    }
})
