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