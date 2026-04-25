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

    fun phaseMap(phase: String, additional: Map<String, Any?> = emptyMap()): Map<String, Any?> = mapOf(
        "message" to phase,
        "phase" to phase,
        "testmints_phase" to phase,
        "testmints" to true,
    ) + additional
}
