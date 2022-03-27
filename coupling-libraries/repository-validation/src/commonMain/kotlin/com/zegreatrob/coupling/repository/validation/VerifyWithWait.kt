package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.testmints.async.Exercise
import kotlinx.coroutines.test.TestResult
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@ExperimentalTime
infix fun <C : Any, R> Exercise<C, R>.verifyWithWait(assertionFunctions: suspend C.(R) -> Unit): TestResult =
    this.verify { result ->
        val timeout = 200
        val mark = TimeSource.Monotonic.markNow()
        var lastError: Throwable? = runAssertions(assertionFunctions, result)
        while (lastError != null && mark.elapsedNow().inWholeMilliseconds < timeout) {
            lastError = runAssertions(assertionFunctions, result)
        }
        if (lastError != null) throw lastError
    }

private suspend fun <C : Any, R> C.runAssertions(
    assertionFunctions: suspend C.(R) -> Unit,
    it: R
) = try {
    assertionFunctions(it)
    null
} catch (t: Throwable) {
    t
}