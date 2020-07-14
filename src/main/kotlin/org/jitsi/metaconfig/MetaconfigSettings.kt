package org.jitsi.metaconfig

class MetaconfigSettings {
    companion object {
        val DefaultLogger = NoOpLogger
        /**
         * A logger for metaconfig to use, if desired.  Defaults
         * to a no-op implementation
         */
        var logger: MetaconfigLogger = DefaultLogger
    }
}

interface MetaconfigLogger {
    fun warn(block: () -> String)
    fun error(block: () -> String)
    fun debug(block: () -> String)
}

val NoOpLogger = object : MetaconfigLogger {
    override fun error(block: () -> String) {}
    override fun warn(block: () -> String) {}
    override fun debug(block: () -> String) {}
}

val StdOutLogger = object : MetaconfigLogger {
    override fun error(block: () -> String) { println("ERROR: ${block()}") }
    override fun warn(block: () -> String) { println("WARN: ${block()}") }
    override fun debug(block: () -> String) { println("DEBUG: ${block()}") }
}
