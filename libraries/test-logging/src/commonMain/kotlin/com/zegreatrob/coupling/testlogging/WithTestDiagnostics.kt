package com.zegreatrob.coupling.testlogging

inline fun <T> withTestDiagnostics(diagnostics: () -> Unit, action: () -> T): T {
    var primaryFailure: Throwable? = null
    try {
        return action()
    } catch (failure: Throwable) {
        primaryFailure = failure
        throw failure
    } finally {
        try {
            diagnostics()
        } catch (failure: Throwable) {
            if (primaryFailure == null) throw failure
            primaryFailure.addSuppressed(failure)
        }
    }
}
