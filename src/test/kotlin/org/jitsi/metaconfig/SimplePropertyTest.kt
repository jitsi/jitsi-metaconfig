package org.jitsi.metaconfig

import MutableMapConfigSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class SimplePropertyTest : ShouldSpec({
    val configSrc = MutableMapConfigSource("test")
    context("a class with a simple property") {
        val obj = object {
            val enabled: Boolean by config("server.enabled".from(configSrc))
        }
        context("when the property is missing") {
            shouldThrow<ConfigException.UnableToRetrieve.NotFound> {
                obj.enabled
            }
        }
        context("when the property is present") {
            configSrc["server.enabled"] = true
            should("read the value correctly") {
                obj.enabled shouldBe true
            }
        }
        context("when the value is the wrong type") {
            configSrc["server.enabled"] = 42
            should("fail to parse") {
                shouldThrow<ConfigException.UnableToRetrieve.WrongType> {
                    obj.enabled
                }
            }
        }
    }
})


