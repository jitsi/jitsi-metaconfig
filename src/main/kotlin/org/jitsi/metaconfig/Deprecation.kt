package org.jitsi.metaconfig

sealed class Deprecation {
    object NotDeprecated : Deprecation()
    sealed class Deprecated(open val msg: String) : Deprecation() {
        class Soft(msg: String) : Deprecated(msg)
        class Hard(msg: String) : Deprecated(msg)
    }
}
