package org.jitsi.metaconfig.supplier

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jitsi.metaconfig.ConfigException
import org.jitsi.metaconfig.ConfigSource
import org.jitsi.metaconfig.noDeprecation
import kotlin.reflect.typeOf

class ConfigSourceSupplierTest : ShouldSpec({
    val configSource = mockk<ConfigSource>().apply {
        every { name } returns "config"
    }

    context("a ConfigSourceSupplier") {
        val css = ConfigSourceSupplier<Int>(
            "some.key",
            configSource,
            typeOf<Int>(),
            noDeprecation()
        )
        context("when the property isn't present") {
            every { configSource.getterFor(typeOf<Int>()) } throws ConfigException.UnableToRetrieve.NotFound("not found")
            shouldThrow<ConfigException.UnableToRetrieve.NotFound> {
                css.get()
            }
        }
    }
})
