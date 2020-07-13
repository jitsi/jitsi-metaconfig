package org.jitsi.metaconfig

class MetaconfigSettings {
    companion object {
        /**
         * A logger for metaconfig to use, if desired.  Defaults
         * to a no-op implementation
         */
        var logger: MetaconfigLogger = NoOpLogger
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
    override fun error(block: () -> String) { println(block()) }
    override fun warn(block: () -> String) { println(block()) }
    override fun debug(block: () -> String) { println(block()) }
}