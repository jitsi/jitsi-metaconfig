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

package org.jitsi.metaconfig

import org.jitsi.metaconfig.supplier.ConfigSourceSupplier
import org.jitsi.metaconfig.supplier.ConfigValueSupplier
import org.jitsi.metaconfig.supplier.TypeConvertingSupplier
import org.jitsi.metaconfig.supplier.ValueTransformingSupplier
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * State classes to aid in the construction of a [ConfigValueSupplier].
 *
 * These classes allow constructing the values needed to build a [ConfigValueSupplier] piece-by-piece, and only
 * allow the creation of a [ConfigValueSupplier] when all the required pieces are present.  Optional operations
 * can be added as well (for doing value or type conversions).
 *
 * Note that currently the construction must be performed in a specific
 * order: (key, source, type [,valueTransformation|typeTransformation]).  And only one transformation
 * (value or type) is allowed.  All of this could be changed by adding the more methods to the different states.
 */
sealed class ConfigPropertyState {
    sealed class Complete<T : Any> : ConfigPropertyState() {
        /**
         * Build a [ConfigValueSupplier].  Required for any subclass of [Complete].
         */
        abstract fun build(): ConfigValueSupplier<T>
        fun softDeprecated(msg: String) = withDeprecation(softDeprecation(msg))
        fun hardDeprecated(msg: String) = withDeprecation(hardDeprecation(msg))
        protected abstract fun withDeprecation(deprecation: Deprecation): Complete<T>

        /**
         * A simple lookup of a property value from the given source as the given type
         */
        class NoTransformation<T : Any>(
            val key: String,
            val source: ConfigSource,
            val type: KType,
            val deprecation: Deprecation
        ) : Complete<T>() {
            /**
             * Add a value transformation operation
             */
            fun andTransformBy(transformer: (T) -> T): ValueTransformation<T> {
                return ValueTransformation(ConfigSourceSupplier(key, source, type, deprecation), transformer)
            }

            /**
             * Add a type conversion operation
             */
            fun <NewType : Any> andConvertBy(converter: (T) -> NewType): TypeConversion<T, NewType> {
                return TypeConversion(ConfigSourceSupplier(key, source, type, deprecation), converter)
            }

            /**
             * We allow updating the type here so that helper functions can fill in the type
             * automatically (since they can derive it from the call) so the caller doesn't have
             * to, however, the helpers will use the final type as a guess; if the caller wants
             * to retrieve the property as another type and then convert it, we need to let them
             * set the retrieved type via this asType call and then use [andConvertBy].  For example:
             * val bool: Boolean by config {
             *     // No need to use 'asType<Boolean>', we can determine that
             *     "app.enabled".from(newConfigSource)
             *     // Here the caller didn't want to retrieve it as bool, so they override the type
             *     // via another call to 'asType' and then convert the value to a Boolean.
             *     "app.enabled".from(newConfigSource).asType<Int>.andConvertBy { it > 0 }
             * }
             */
            inline fun <reified R : Any> asType(): NoTransformation<R> {
                return NoTransformation(key, source, typeOf<R>(), noDeprecation())
            }

            override fun build(): ConfigValueSupplier<T> {
                return ConfigSourceSupplier(key, source, type, deprecation)
            }

            override fun withDeprecation(deprecation: Deprecation) =
                NoTransformation<T>(key, source, type, deprecation)
        }

        /**
         * A lookup which transforms the value in some way
         */
        class ValueTransformation<T : Any>(
            val innerSupplier: ConfigValueSupplier<T>,
            val transformer: (T) -> T
        ) : Complete<T>() {
            override fun build(): ConfigValueSupplier<T> {
                return ValueTransformingSupplier(innerSupplier, transformer)
            }

            override fun withDeprecation(deprecation: Deprecation) =
                ValueTransformation(innerSupplier.withDeprecation(deprecation), transformer)
        }

        /**
         * A lookup which converts the type the value was retrieved as to another type
         */
        class TypeConversion<OriginalType : Any, NewType : Any>(
            val innerSupplier: ConfigValueSupplier<OriginalType>,
            val converter: (OriginalType) -> NewType
        ) : Complete<NewType>() {
            override fun build(): ConfigValueSupplier<NewType> {
                return TypeConvertingSupplier(innerSupplier, converter)
            }

            override fun withDeprecation(deprecation: Deprecation) =
                TypeConversion(innerSupplier.withDeprecation(deprecation), converter)
        }
    }

    sealed class Incomplete : ConfigPropertyState() {
        /**
         * The initial empty state
         */
        object Empty : Incomplete() {
            fun lookup(key: String): KeyOnly = KeyOnly(key)
        }

        /**
         * Only the config key is present
         */
        class KeyOnly(val key: String) : Incomplete() {
            fun from(configSource: ConfigSource): KeyAndSource {
                return KeyAndSource(key, configSource, noDeprecation())
            }
        }

        /**
         * The config key and source are present
         */
        class KeyAndSource(val key: String, val source: ConfigSource, val deprecation: Deprecation) : Incomplete() {
            inline fun <reified T : Any> asType(): Complete.NoTransformation<T> {
                return Complete.NoTransformation(key, source, typeOf<T>(), deprecation)
            }
            fun <T : Any> asType(type: KType): Complete.NoTransformation<T> {
                return Complete.NoTransformation(key, source, type, deprecation)
            }

            // These don't make sense until we have at least a key and a source, so put them in
            // KeyAndSource instead of defining at Incomplete (or at ConfigPropertyState)
            fun softDeprecated(msg: String) = withDeprecation(softDeprecation(msg))
            fun hardDeprecated(msg: String) = withDeprecation(hardDeprecation(msg))
            protected fun withDeprecation(deprecation: Deprecation) =
                KeyAndSource(key, source, deprecation)
        }
    }
}
