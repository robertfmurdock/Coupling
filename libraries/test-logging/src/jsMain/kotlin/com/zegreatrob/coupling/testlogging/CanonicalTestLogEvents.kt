package com.zegreatrob.coupling.testlogging

import kotlin.js.Json
import kotlin.js.json

object CanonicalTestLogEvents {
    const val TEST_MINTS_LOGGER = "testmints"

    fun phaseJson(phase: String, additional: Json = json()): Json = json(
        "testmints" to true,
        "phase" to phase,
        "testmints_phase" to phase,
    ).add(additional)
}
