package org.jitsi

import org.jitsi.metaconfig.MetaconfigLogger

class MockLogger(val printToStdOut: Boolean = false) : MetaconfigLogger {
    val errorMessages = mutableListOf<String>()
    val warnMessages = mutableListOf<String>()
    val debugMessages = mutableListOf<String>()

    override fun debug(block: () -> String) {
        block().apply {
            if (printToStdOut) {
                println("DEBUG: $this")
            }
            debugMessages += this
        }
    }

    override fun error(block: () -> String) {
        block().apply {
            if (printToStdOut) {
                println("ERROR: $this")
            }
            errorMessages += this
        }
    }

    override fun warn(block: () -> String) {
        block().apply {
            if (printToStdOut) {
                println("WARN: $this")
            }
            warnMessages += this
        }
    }

}
