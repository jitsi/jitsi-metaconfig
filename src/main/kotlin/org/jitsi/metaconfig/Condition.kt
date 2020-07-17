package org.jitsi.metaconfig

data class Condition(
    val context: String,
    private val predicate: () -> Boolean
) {
    fun enabled(): Boolean = predicate()
}

val AlwaysEnabled = Condition("") { true }
