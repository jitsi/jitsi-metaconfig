package org.jitsi.metaconfig

sealed class Deprecation {
    object NotDeprecated : Deprecation()
    sealed class Deprecated(open val msg: String) : Deprecation() {
        class Soft(msg: String) : Deprecated(msg)
        class Hard(msg: String) : Deprecated(msg)
    }
}

fun noDeprecation() = Deprecation.NotDeprecated
fun softDeprecation(msg: String) = Deprecation.Deprecated.Soft(msg)
fun hardDeprecation(msg: String) = Deprecation.Deprecated.Hard(msg)
