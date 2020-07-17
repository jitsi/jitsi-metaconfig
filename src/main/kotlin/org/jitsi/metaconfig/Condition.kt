package org.jitsi.metaconfig

data class Condition(
    val context: String,
    private val predicate: () -> Boolean
) {
    fun isMet(): Boolean {
        return try {
            predicate()
        } catch (t: Throwable) {
            // Any exception while evaluating the predicate results in the condition failing
            false
        }
    }
}

val AlwaysEnabled = Condition("") { true }
