package org.jitsi.metaconfig.supplier

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

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
    }
})
