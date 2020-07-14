package org.jitsi.metaconfig.supplier

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jitsi.metaconfig.ConfigException
import org.jitsi.metaconfig.ConfigSource
import kotlin.reflect.typeOf

class ConfigSourceSupplierTest : ShouldSpec({
    val configSource: ConfigSource = mockk()

    context("a ConfigSourceSupplier") {
        val css = ConfigSourceSupplier<Int>(
            "some.key",
            configSource,
            typeOf<Int>()
        )
        should("query the source every time when accessed") {
            every { configSource.getterFor(typeOf<Int>()) } returns { 42 }

            css.get() shouldBe 42
            css.get() shouldBe 42
            verify(exactly = 2) { configSource.getterFor(typeOf<Int>()) }
        }
        context("when the property isn't present") {
            every { configSource.getterFor(typeOf<Int>()) } throws ConfigException.UnableToRetrieve.NotFound("not found")
            shouldThrow<ConfigException.UnableToRetrieve.NotFound> {
                css.get()
            }
        }
    }
})
