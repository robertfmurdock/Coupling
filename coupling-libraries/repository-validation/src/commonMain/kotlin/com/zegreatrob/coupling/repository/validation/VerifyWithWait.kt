package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.testmints.async.Exercise
import com.zegreatrob.testmints.async.Verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestResult
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@ExperimentalCoroutinesApi
@ExperimentalTime
infix fun <C : Any, R> Exercise<C, R>.verifyWithWait(assertionFunctions: suspend C.(R) -> Unit): TestResult =
    this.verify { result -> vWW(assertionFunctions, result) }

@ExperimentalTime
infix fun <C : Any, R> Exercise<C, R>.verifyWithWaitAnd(assertionFunctions: suspend C.(R) -> Unit): Verify<C, R?> =
    this.verifyAnd { result -> vWW(assertionFunctions, result) }

@ExperimentalTime
private suspend fun <C : Any, R> C.vWW(assertionFunctions: suspend C.(R) -> Unit, result: R) {
    val timeout = 2000
    val mark = TimeSource.Monotonic.markNow()
    var lastError: Throwable? = runAssertions(assertionFunctions, result)
    var retryCount = 0
    while (lastError != null && mark.elapsedNow().inWholeMilliseconds < timeout) {
        println("RETRYING ${retryCount++} elapsed ${mark.elapsedNow().inWholeMilliseconds}ms")
        lastError = runAssertions(assertionFunctions, result)
        delay(20)
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
