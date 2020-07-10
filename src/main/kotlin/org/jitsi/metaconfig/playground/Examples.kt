package org.jitsi.metaconfig.playground

import org.jitsi.metaconfig.ConfigPropertyNotFoundException
import org.jitsi.metaconfig.ConfigSource
import java.time.Duration
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf


//@Suppress("UNCHECKED_CAST")
//private class ConfigDelegate<T : Any>(private val key: String, source: ConfigSource, type: KType) {
//    private val getter: (String) -> T = source.getterFor(type) as (String) -> T
//
//    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
//        return getter(key)
//    }
//}
//
//@Suppress("UNCHECKED_CAST")
//private class AggregateConfigDelegate<T : Any>(private val props: List<ConfigProperty>) {
//    private val getters = props.map {
//        val x = it.source.getterFor(it.type);
//        {
//            x(it.key) as T
//        }
////        it.source.getterFor(it.type)(it.key) as () -> T
//    }
//
//    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
//        for (getter in getters) {
//            try {
//                return getter()
//            } catch (e: ConfigPropertyNotFoundException) {}
//        }
//        throw ConfigPropertyNotFoundException("prop not found from any source")
//    }
//}
//
//@OptIn(ExperimentalStdlibApi::class)
//private inline fun <reified T : Any> config(propKey: String): ConfigDelegate<T> {
//    return ConfigDelegate<T>(propKey, newConfigSource, typeOf<T>())
//}
//
//@OptIn(ExperimentalStdlibApi::class)
//private inline fun <reified T : Any> config(vararg props: ConfigProperty): AggregateConfigDelegate<T> {
//    return AggregateConfigDelegate<T>(props.toList())
//}
//
//// We need something more flexible here, as we want to be able to have ConfigProperties which work in different
//// ways (like one that transforms a retrieved value, not just retrieves it).  OR...we leave this as-is and
//// create some sort of type which can have a 'pipeline' of things?  retriever, transformer, etc. ?
//data class ConfigProperty(
//    val key: String,
//    val source: ConfigSource,
//    val type: KType
//)
//
//@ExperimentalStdlibApi
//inline fun <reified T : Any> newconfig(keyPath: String): ConfigProperty = ConfigProperty(keyPath, newConfigSource, typeOf<T>())
//
//@ExperimentalStdlibApi
//inline fun <reified T : Any> legacyconfig(keyPath: String): ConfigProperty = ConfigProperty(keyPath, legacyConfigSource, typeOf<T>())
//
//@ExperimentalStdlibApi
//inline fun <reified T : Any> ConfigProperty.retrievedAs() : ConfigProperty {
//    return ConfigProperty(this.key, this.source, typeOf<T>())
//}
//
////fun <T : Any, R : Any>ConfigProperty.transformedBy(transformer: (T) -> R):  {
////
////}
//
//
//
