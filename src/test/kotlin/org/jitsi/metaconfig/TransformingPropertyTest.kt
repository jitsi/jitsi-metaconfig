package org.jitsi.metaconfig

import MutableMapConfigSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class TransformingPropertyTest : ShouldSpec({
    val configSrc = MutableMapConfigSource("test")

    context("a class with a property whose value is transformed") {
        val obj = object {
            val enabled: Boolean by config {
                retrieve("disabled".from(configSrc).andTransformBy { !it })
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