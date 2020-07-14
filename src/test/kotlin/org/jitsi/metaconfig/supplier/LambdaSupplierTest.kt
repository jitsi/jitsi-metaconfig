package org.jitsi.metaconfig.supplier

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

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
    }
})
