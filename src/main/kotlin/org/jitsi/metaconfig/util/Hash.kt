package org.jitsi.metaconfig.util

/**
 * Compute a hashcode of multiple values
 */
inline fun hash(vararg vals: Any?): Int {
    var res = 0
    for (v in vals) {
        res += v.hashCode()
        res *= 31
    }
    return res
}
