package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.testlogging.CanonicalTestLogEvents
import com.zegreatrob.testmints.report.MintReporter
import com.zegreatrob.testmints.report.MintReporterConfig
import kotlin.js.Json
import kotlin.js.json

class E2eJsonlTestMintsReporter private constructor() : MintReporter {

    override fun exerciseStart(context: Any) {
        emitPhase("setup-finish", json("context" to context.toString()))
        emitPhase("exercise-start")
    }

    override fun exerciseFinish() = emitPhase("exercise-finish")

    override fun verifyStart(payload: Any?) = emitPhase(
        "verify-start",
        payload?.toString()?.let { json("payload" to it) } ?: json(),
    )

    override fun verifyFinish() = emitPhase("verify-finish")

    private fun emitPhase(phase: String, additionalProperties: Json = json()) {
        appendCanonicalToTestLog(
            logger = CanonicalTestLogEvents.TEST_MINTS_LOGGER,
            message = phase,
            properties = CanonicalTestLogEvents.phaseJson(phase, additionalProperties),
        )
    }

    companion object {
        fun emitTestStart() = emitCanonicalPhase("test-start")

        fun emitTestFinish() = emitCanonicalPhase("test-finish")

        fun emitSetupStart() {
            emitCanonicalPhase("setup-start")
        }

        fun initialize() {
            MintReporterConfig.reporter = E2eJsonlTestMintsReporter()
        }

        private fun emitCanonicalPhase(phase: String) {
            appendCanonicalToTestLog(
                logger = CanonicalTestLogEvents.TEST_MINTS_LOGGER,
                message = phase,
                properties = CanonicalTestLogEvents.phaseJson(phase),
            )
        }
    }
}
