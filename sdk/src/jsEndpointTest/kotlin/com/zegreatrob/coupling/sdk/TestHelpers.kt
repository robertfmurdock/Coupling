package com.zegreatrob.coupling.sdk

inline fun <T> catchException(function: () -> T): Exception? = try {
    function()
    null
} catch (result: Exception) {
    result
}