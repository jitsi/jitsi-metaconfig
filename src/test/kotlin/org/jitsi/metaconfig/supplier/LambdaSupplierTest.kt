package org.jitsi.metaconfig.supplier

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jitsi.metaconfig.ConfigException

class LambdaSupplierTest : ShouldSpec({
    var lambdaCallCount = 0
    val lambda = {
        lambdaCallCount++
        42
    }

    context("a LambdaSupplier") {
        val ls = LambdaSupplier(lambda)
        should("get the value from the given lambda") {
            ls.get() shouldBe 42
            lambdaCallCount shouldBe 1
        }
        context("with a context hint") {
            val lsc = LambdaSupplier("hard-coded value of 42", lambda)
            should("include the context in the toString") {
                lsc.toString() shouldContain "hard-coded value of 42"
            }
        }
        context("that throws any kind of exception") {
            val lsc = LambdaSupplier<Int> { throw RuntimeException() }
            should("throw NotFound") {
                shouldThrow<ConfigException.UnableToRetrieve.NotFound> {
                    lsc.get()
                }
            }
        }
    }
})
