package org.jitsi.metaconfig.supplier

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import org.jitsi.MockLogger
import org.jitsi.metaconfig.ConfigException
import org.jitsi.metaconfig.MapConfigSource
import org.jitsi.metaconfig.MetaconfigSettings
import org.jitsi.metaconfig.hardDeprecation
import org.jitsi.metaconfig.softDeprecation
import kotlin.reflect.typeOf

class ConfigValueSupplierTest : ShouldSpec({
    val mockLogger = MockLogger()
    val config = MapConfigSource("config") {
        put("new.num", 42)
    }
    beforeSpec {
        MetaconfigSettings.logger = mockLogger
    }

    afterSpec {
        MetaconfigSettings.logger = MetaconfigSettings.DefaultLogger
    }

    context("A ConfigValueSupplier") {
        should("cache the retrieved value") {
            var supplierCallCount = 0
            val s = LambdaSupplier("foo") {
                supplierCallCount++
                42
            }
            s.get() shouldBe 42
            supplierCallCount shouldBe 1
            s.get() shouldBe 42
            supplierCallCount shouldBe 1
        }
        should("throw the exception every time if it isn't found") {
            var supplierCallCount = 0
            val s = LambdaSupplier<Int>("foo") {
                supplierCallCount++
                throw ConfigException.UnableToRetrieve.NotFound("not found")
            }
            shouldThrow<ConfigException.UnableToRetrieve.NotFound> {
                s.get()
            }
            shouldThrow<ConfigException.UnableToRetrieve.NotFound> {
                s.get()
            }
        }
        context("marked as soft deprecated") {
            context("that finds a value") {
                val s = ConfigSourceSupplier<Int>(
                    "new.num", config, typeOf<Int>(), softDeprecation("deprecated"))
                should("log a warning") {
                    s.get() shouldBe 42
                    mockLogger.warnMessages.any {
                        it.contains(Regex(".*A value was retrieved via .* which is deprecated: deprecated"))
                    } shouldBe true
                }
            }
            context("that doesn't find a value") {
                val s = ConfigSourceSupplier<Int>(
                    "missing.num", config, typeOf<Int>(), softDeprecation("deprecated"))
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
                val s = ConfigSourceSupplier<Int>(
                    "new.num", config, typeOf<Int>(), hardDeprecation("deprecated"))
                should("throw an exception") {
                    val ex = shouldThrow<ConfigException.UnableToRetrieve.Deprecated> {
                        s.get()
                    }
                    ex.message.shouldMatch(Regex(".*A value was retrieved via .* which is deprecated: deprecated"))
                }
            }
            context("that doesn't find a value") {
                val s = ConfigSourceSupplier<Int>(
                    "missing.num", config, typeOf<Int>(), hardDeprecation("deprecated"))
                should("throw the UnableToRetrieve exception") {
                    shouldThrow<ConfigException.UnableToRetrieve.NotFound> {
                        s.get()
                    }
                }
            }
        }
    }
})
