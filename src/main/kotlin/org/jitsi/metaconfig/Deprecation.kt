package org.jitsi.metaconfig

sealed class Deprecation {
    object NotDeprecated : Deprecation()
    sealed class Deprecated(open val msg: String) : Deprecation() {
        class Soft(msg: String) : Deprecated(msg)
        class Hard(msg: String) : Deprecated(msg)
    }
}

fun notDeprecated() = Deprecation.NotDeprecated
fun softDeprecated(msg: String) = Deprecation.Deprecated.Soft(msg)
fun hardDeprecated(msg: String) = Deprecation.Deprecated.Hard(msg)
