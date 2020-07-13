package org.jitsi.metaconfig

import MutableMapConfigSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class FallbackPropertyTest : ShouldSpec({
    val firstConfigSource = MutableMapConfigSource("legacy config")
    val secondConfigSource = MutableMapConfigSource("new config")

    context("a class with a fallback property") {
        val obj = object {
            val enabled: Boolean by config {
                retrieve("old.path.enabled".from(firstConfigSource))
                retrieve("server.enabled".from(secondConfigSource))
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