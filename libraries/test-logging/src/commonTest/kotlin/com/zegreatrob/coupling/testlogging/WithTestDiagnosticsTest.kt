package com.zegreatrob.coupling.testlogging

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class WithTestDiagnosticsTest {
    @Test
    fun collectsDiagnosticsAfterSuccessfulAction() = setup(object {
        val events = mutableListOf<String>()
    }) exercise {
        withTestDiagnostics({ events.add("diagnostics") }) {
            events.add("action")
            "result"
        }
    } verify { result ->
        result.assertIsEqualTo("result")
        events.assertIsEqualTo(listOf("action", "diagnostics"))
    }

    @Test
    fun collectsDiagnosticsWhenBootstrapFails() = setup(object {
        val failure = IllegalStateException("bootstrap failed")
        var collections = 0
    }) exercise {
        runCatching { withTestDiagnostics({ collections++ }) { throw failure } }
    } verify { result ->
        result.exceptionOrNull().assertIsEqualTo(failure)
        collections.assertIsEqualTo(1)
    }

    @Test
    fun preservesActionFailureWhenDiagnosticsAlsoFail() = setup(object {
        val failure = IllegalStateException("page did not render")
        val diagnosticFailure = IllegalArgumentException("browser disconnected")
    }) exercise {
        runCatching { withTestDiagnostics({ throw diagnosticFailure }) { throw failure } }
    } verify { result ->
        result.exceptionOrNull().assertIsEqualTo(failure)
        failure.suppressedExceptions.assertIsEqualTo(listOf(diagnosticFailure))
    }

    @Test
    fun failsSuccessfulActionWhenLogChecksFail() = setup(object {
        val failure = IllegalStateException("unexpected browser error")
    }) exercise {
        runCatching { withTestDiagnostics({ throw failure }) { "result" } }
    } verify { result ->
        result.exceptionOrNull().assertIsEqualTo(failure)
    }
}
