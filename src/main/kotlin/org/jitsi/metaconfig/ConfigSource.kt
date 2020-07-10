package org.jitsi.metaconfig

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf

/**
 * A [ConfigSource] is what is used to retrieve configuration values
 * from some location.
 */
interface ConfigSource {
    /**
     * Given a [type], return a function which takes in a
     * configuration property key (aka a key 'name') and returns the value
     * of the property at the given name as the type referred to by [type].
     *
     * The return getter should return the value corresponding to the given
     * key, or throw [ConfigPropertyNotFoundException].
     */
    fun getterFor(type: KType): (String) -> Any

    /**
     * A name for this [ConfigSource] to give extra context in the
     * event of errors
     */
    val name: String
}

@Suppress("UNCHECKED_CAST")
@ExperimentalStdlibApi
inline fun <reified T : Any> ConfigSource.getterFor(): (String) -> T {
    return getterFor(typeOf<T>()) as (String) -> T
}

class ConfigPropertyNotFoundException(msg: String) : Exception(msg)


//@OptIn(ExperimentalStdlibApi::class)
//fun foo(type: KType): Any {
//    println(type)
//    when (type) {
//        typeOf<Int>() -> {
//            println("got an int")
//            return 42
//        }
//        typeOf<List<List<Int>>>() -> {
//            println("got List<List<Int>>")
//            return listOf(listOf(1))
//        }
//    }
//    TODO()
//}
//
//@OptIn(ExperimentalStdlibApi::class)
//inline fun <reified T : Any> bar(): T {
//    return foo(typeOf<T>()) as T
//}
//
//@ExperimentalStdlibApi
//fun main() {
//    println(bar<Int>())
////    foo(typeOf<Boolean>())
////    foo(typeOf<List<Int>>())
//    println(bar<List<List<Int>>>())
//}
//
