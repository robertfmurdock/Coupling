package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.js.json
import kotlin.test.Test

class BrowserLogPolicyTest {
    @Test
    fun bootstrapLogFailureIsMarkedForOneDiagnosticBundle() = asyncSetup(object {
        val failure = IllegalStateException("startup warning")
        val marked = mutableListOf<Throwable>()
    }) exercise {
        runCatching {
            withBootstrapDiagnostics(diagnostics = { throw failure }, markFailure = { marked.add(it) }) { Unit }
        }
    } verify { result ->
        result.exceptionOrNull().assertIsEqualTo(failure)
        marked.assertIsEqualTo(listOf(failure))
    }

    @Test
    fun warningFailsTheLogCheck() = asyncSetup(object {
        val entries = listOf(json("level" to "WARN", "message" to "unexpected warning"))
    }) exercise {
        runCatching { checkLogs { entries } }
    } verify { result ->
        result.exceptionOrNull()?.message?.contains("unexpected warning").assertIsEqualTo(true)
    }

    @Test
    fun errorFailsTheLogCheck() = asyncSetup(object {
        val entries = listOf(json("level" to "ERROR", "message" to "unexpected error"))
    }) exercise {
        runCatching { checkLogs { entries } }
    } verify { result ->
        result.exceptionOrNull()?.message?.contains("unexpected error").assertIsEqualTo(true)
    }

    @Test
    fun informationalOutputPassesTheLogCheck() = asyncSetup(object {
        val entries = listOf(json("level" to "INFO", "message" to "information"))
    }) exercise {
        runCatching { checkLogs { entries } }
    } verify { result ->
        result.getOrThrow().assertIsEqualTo(Unit)
    }
}
