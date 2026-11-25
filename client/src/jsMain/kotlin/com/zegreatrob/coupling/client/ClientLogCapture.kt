package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.logging.JsonFormatter
import com.zegreatrob.coupling.logging.Message
import io.github.oshai.kotlinlogging.Appender
import io.github.oshai.kotlinlogging.KLoggingEvent
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.js.Date

internal object ClientLogCapture {
    private var installed = false
    private var consoleAppender: Appender? = null
    private var version = "unknown"
    private val history = ClientLogHistory()

    fun install(clientVersion: String) {
        version = clientVersion
        if (installed) return
        val originalAppender = KotlinLoggingConfiguration.direct.appender
        consoleAppender = originalAppender
        KotlinLoggingConfiguration.direct.appender = object : Appender {
            override fun log(loggingEvent: KLoggingEvent) {
                originalAppender.log(loggingEvent)
                captureAction(loggingEvent)
            }
        }
        installed = true
        history.read(Date.now().toLong())
    }

    fun clear() = history.clear()

    fun uninstall() {
        if (!installed) return
        consoleAppender?.let { KotlinLoggingConfiguration.direct.appender = it }
        consoleAppender = null
        installed = false
    }

    fun statusSummary(nowMillis: Long = Date.now().toLong()): String {
        val count = history.read(nowMillis).size
        return "$count saved client log events. Storage: ${history.status}. " +
            "History expires after 30 days and may be shortened by the 1 MiB storage cap."
    }

    fun export(nowMillis: Long = Date.now().toLong()): String {
        val records = history.read(nowMillis)
        val metadata = buildJsonObject {
            put("type", "client-log-export")
            put("exportedAt", Date(nowMillis.toDouble()).toISOString())
            put("retainedFrom", records.firstOrNull()?.message?.timestamp ?: "unknown")
            put("retainedTo", records.lastOrNull()?.message?.timestamp ?: "unknown")
            put("clientVersions", JsonArray(records.map { it.clientVersion }.distinct().map(::JsonPrimitive)))
            put("recordCount", records.size)
            put("droppedCount", history.droppedCount)
            put("expiredCount", history.expiredCount)
            put("capacityEvictedCount", history.evictedCount)
            put("counterCoverage", "counts observed by this document only; other tabs and earlier sessions are unknown")
            put("storageStatus", history.status)
        }
        return (listOf(metadata.toString()) + records.map { Json.encodeToString(it) }).joinToString("\n")
    }

    private fun captureAction(event: KLoggingEvent) {
        if (event.loggerName != "ActionLogger") return
        runCatching {
            val formatted = JsonFormatter.formatMessage(event)
            val message = Json.decodeFromString<Message>(formatted)
            val safePayload = event.payload?.filterKeys { it in safeActionFields }
            history.append(
                message = message.copy(message = safePayload?.toString() ?: message.message),
                clientVersion = version,
                emittedAtMillis = Date.now().toLong(),
                structuredProperties = safePayload?.let(::typedProperties),
            )
        }
    }

    private fun typedProperties(payload: Map<String, Any?>) = buildJsonObject {
        payload.forEach { (name, value) ->
            put(name, value.toJsonElement())
        }
    }

    private fun Any?.toJsonElement(): JsonElement = when (this) {
        null -> JsonNull
        is Boolean -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        else -> JsonPrimitive(toString())
    }

    private val safeActionFields = setOf(
        "command_action",
        "command_phase",
        "command_trace_id",
        "command_duration_ms",
        "command_error",
        "command_cancelled",
        "action",
        "type",
        "duration",
        "traceId",
    )
}
