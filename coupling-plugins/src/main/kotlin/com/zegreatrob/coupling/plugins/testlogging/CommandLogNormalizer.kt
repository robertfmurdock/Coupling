package com.zegreatrob.coupling.plugins.testlogging

internal object CommandLogNormalizer {
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
        val propertyAction = properties["command_action"]?.toString()
            ?: properties["action"]?.toString()
        val propertyPhase = properties["command_phase"]?.toString()
            ?: properties["type"]?.toString()?.lowercase()
        val propertyTraceId = properties["command_trace_id"]?.toString()
            ?: properties["traceId"]?.toString()
        val propertyDurationMs = numericDurationMs(properties["command_duration_ms"]?.toString())
            ?: numericDurationMs(properties["duration"]?.toString())

        val messageAction = commandValue(message, "command_action")
            ?: commandValue(message, "action")
        val messagePhase = commandValue(message, "command_phase")
            ?: commandValue(message, "type")?.lowercase()
        val messageTraceId = commandValue(message, "command_trace_id")
            ?: commandValue(message, "traceId")
        val messageDurationMs = numericDurationMs(commandValue(message, "command_duration_ms"))
            ?: numericDurationMs(commandValue(message, "duration"))

        val action = propertyAction ?: messageAction
        val phase = propertyPhase ?: messagePhase
        if (action.isNullOrBlank() || phase.isNullOrBlank()) {
            return NormalizedLog(
                logger = loggerName,
                message = message,
                properties = properties,
            )
        }

        val normalizedPhase = when (phase.lowercase()) {
            "start" -> "start"
            "end" -> "end"
            else -> phase.lowercase()
        }
        val enriched = properties.toMutableMap().apply {
            put("command", true)
            put("command_action", action)
            put("command_phase", normalizedPhase)
            (propertyTraceId ?: messageTraceId)?.let { put("command_trace_id", it) }
            (propertyDurationMs ?: messageDurationMs)?.let { put("command_duration_ms", it) }
        }

        return NormalizedLog(
            logger = "command",
            message = "$action:$normalizedPhase",
            properties = enriched,
        )
    }

    private fun numericDurationMs(raw: String?): Double? {
        if (raw.isNullOrBlank()) {
            return null
        }
        return when {
            raw.endsWith("ms") -> raw.removeSuffix("ms").toDoubleOrNull()
            raw.endsWith("s") -> raw.removeSuffix("s").toDoubleOrNull()?.times(1000.0)
            else -> raw.toDoubleOrNull()
        }
    }

    private fun commandValue(message: String, key: String): String? {
        val regex = Regex("""\b${Regex.escape(key)}=([^,}\s]+)""")
        return regex.find(message)?.groupValues?.get(1)?.trim()
    }
}
