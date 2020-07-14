package org.jitsi.metaconfig.supplier

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.jitsi.metaconfig.ConfigException

class ValueTransformingSupplierTest : ShouldSpec({
    val workingOrigSupplier = LambdaSupplier{ 42 }

    val missingOrigSupplier = LambdaSupplier<Int> {
        throw ConfigException.UnableToRetrieve.NotFound("not found")
    }

    context("a ValueTransformingSUpplier") {
        context("with an inner supplier which finds the value") {
            val tcs = ValueTransformingSupplier(workingOrigSupplier) { it + 1 }
            should("convert the type correctly") {
                tcs.get() shouldBe 43
            }
        }
        context("with an inner supplier which doesn't find the value") {
            val tcs = ValueTransformingSupplier(missingOrigSupplier) { it + 1 }
            shouldThrow<ConfigException.UnableToRetrieve.NotFound> {
                tcs.get()
            }
        }
    }
})
