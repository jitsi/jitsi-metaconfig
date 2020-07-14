package org.jitsi.metaconfig.supplier

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldMatch
import org.jitsi.MockLogger
import org.jitsi.metaconfig.ConfigException
import org.jitsi.metaconfig.MetaconfigSettings
import org.jitsi.metaconfig.hardDeprecated
import org.jitsi.metaconfig.softDeprecated

class ConfigValueSupplierTest : ShouldSpec({
    val mockLogger = MockLogger()
    beforeSpec {
        MetaconfigSettings.logger = mockLogger
    }

    afterSpec {
        MetaconfigSettings.logger = MetaconfigSettings.DefaultLogger
    }

    context("A ConfigValueSupplier") {
        context("marked as soft deprecated") {
            context("that finds a value") {
                val s = LambdaSupplier(softDeprecated("deprecated")) { 42 }
                should("log a warning") {
                    s.get() shouldBe 42
                    mockLogger.warnMessages.any {
                        it.contains(Regex(".*A value was retrieved via .* which is deprecated: deprecated"))
                    } shouldBe true
                }
            }
            context("that doesn't find a value") {
                val s = LambdaSupplier<Int>(softDeprecated("deprecated")) {
                    throw ConfigException.UnableToRetrieve.NotFound("not found")
                }
                should("not log a warning") {
                    shouldThrow<ConfigException.UnableToRetrieve.NotFound> {
                        s.get()
                    }
                    mockLogger.warnMessages.none {
                        it.contains(Regex(".*A value was retrieved via .* which is deprecated: deprecated"))
                    } shouldBe true
                }
            }
        }
        context("marked as hard deprecated") {
            context("that finds a value") {
                val s = LambdaSupplier(hardDeprecated("deprecated")) { 42 }
                should("throw an exception") {
                    val ex = shouldThrow<ConfigException.UnableToRetrieve.Deprecated> {
                        s.get()
                    }
                    ex.message.shouldMatch(Regex(".*A value was retrieved via .* which is deprecated: deprecated"))
                }
            }
            context("that doesn't find a value") {
                val s = LambdaSupplier<Int>(hardDeprecated("deprecated")) {
                    throw ConfigException.UnableToRetrieve.NotFound("not found")
                }
                should("throw the UnableToRetrieve exception") {
                    shouldThrow<ConfigException.UnableToRetrieve.NotFound> {
                        s.get()
                    }
                }
            }
        }
    }
})
