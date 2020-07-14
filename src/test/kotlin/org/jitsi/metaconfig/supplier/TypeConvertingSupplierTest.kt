package org.jitsi.metaconfig.supplier

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.jitsi.metaconfig.ConfigException

class TypeConvertingSupplierTest : ShouldSpec({
    val workingOrigSupplier = LambdaSupplier{ 42 }

    val missingOrigSupplier = LambdaSupplier<Int> {
        throw ConfigException.UnableToRetrieve.NotFound("not found")
    }

    context("a TypeConvertingSupplier") {
        context("with an inner supplier which finds the value") {
            val tcs = TypeConvertingSupplier(workingOrigSupplier) { it > 0 }
            should("convert the type correctly") {
                tcs.get() shouldBe true
            }
        }
        context("with an inner supplier which doesn't find the value") {
            val tcs = TypeConvertingSupplier(missingOrigSupplier) { it > 0 }
            shouldThrow<ConfigException.UnableToRetrieve.NotFound> {
                tcs.get()
            }
        }
    }
})
