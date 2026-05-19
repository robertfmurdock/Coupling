package com.zegreatrob.coupling.plugins.testlogging

internal object TestmintsLogNormalizer {
    private val canonicalTestMintsPhases = listOf(
        "setup-start",
        "setup-finish",
        "exercise-start",
        "exercise-finish",
        "verify-start",
        "verify-finish",
        "test-start",
        "test-finish",
    )

    data class NormalizedLog(
        val logger: String,
        val message: String,
        val properties: Map<String, Any?>,
    )

    fun normalize(
        loggerName: String,
        message: String,
        properties: Map<String, Any?>,
    ): NormalizedLog {
        val phase = testmintsPhase(message)
        val isTestmints = loggerName.equals("testmints", ignoreCase = true) ||
            phase != null ||
            message.contains("[testmints]") ||
            message.contains("testmints -")
        if (!isTestmints) {
            return NormalizedLog(
                logger = loggerName,
                message = message,
                properties = properties,
            )
        }

        val step = testmintsValue(message, "step")
        val state = testmintsValue(message, "state")
        val name = testmintsName(message)
        val enriched = properties.toMutableMap().apply {
            put("testmints", true)
            phase?.let { put("testmints_phase", it) }
            step?.let { put("testmints_step", it) }
            state?.let { put("testmints_state", it) }
            name?.let { put("testmints_name", it) }
        }

        return NormalizedLog(
            logger = "testmints",
            message = phase ?: message,
            properties = enriched,
        )
    }

    private fun testmintsPhase(message: String): String? {
        canonicalTestMintsPhases.firstOrNull { message.contains(it) }?.let { return it }

        return when {
            message.contains("setupStart") -> "setup-start"
            message.contains("setupFinish") -> "setup-finish"
            message.contains("exerciseStart") -> "exercise-start"
            message.contains("exerciseFinish") -> "exercise-finish"
            message.contains("verifyStart") -> "verify-start"
            message.contains("verifyFinish") -> "verify-finish"
            message.contains("testStart") -> "test-start"
            message.contains("testFinish") -> "test-finish"
            else -> null
        }
    }

    private fun testmintsValue(message: String, key: String): String? {
        val regex = Regex("""\\b${Regex.escape(key)}=([^,}\\s]+)""")
        return regex.find(message)?.groupValues?.get(1)
    }

    private fun testmintsName(message: String): String? {
        val regex = Regex("""\\bname=([^,}]+)""")
        return regex.find(message)?.groupValues?.get(1)?.trim()
    }
}
