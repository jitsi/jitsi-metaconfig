package org.jitsi.metaconfig.supplier

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.jitsi.metaconfig.Condition
import org.jitsi.metaconfig.ConfigException

class ConditionalSupplierTest : ShouldSpec({
    context("a conditional supplier") {
        context("when the condition is met") {
            val cs = ConditionalSupplier<Int>(
                Condition("enabled", { true }),
                listOf(
                    LambdaSupplier { 42 }
                )
            )
            should("return the value") {
                cs.get() shouldBe 42
            }
        }
        context("when the condition is not met") {
            val cs = ConditionalSupplier<Int>(
                Condition("enabled", { false }),
                listOf(
                    LambdaSupplier { 42 }
                )
            )
            should("throw an exception") {
                shouldThrow<ConfigException.UnableToRetrieve.ConditionNotMet> {
                    cs.get()
                }
            }
        }
    }
})
