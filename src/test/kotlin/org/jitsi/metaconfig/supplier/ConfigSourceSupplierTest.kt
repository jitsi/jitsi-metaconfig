/*
 * Copyright @ 2018 - present 8x8, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jitsi.metaconfig.supplier

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.every
import io.mockk.mockk
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
