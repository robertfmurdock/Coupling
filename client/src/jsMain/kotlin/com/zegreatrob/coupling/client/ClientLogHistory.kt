package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.logging.JsonFormatter
import com.zegreatrob.coupling.logging.Message
import io.github.oshai.kotlinlogging.Appender
import io.github.oshai.kotlinlogging.KLoggingEvent
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import kotlinx.browser.localStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.js.Date
import kotlin.uuid.Uuid

internal interface ClientLogStorage {
    fun keys(): List<String>
    fun get(key: String): String?
    fun put(key: String, value: String)
    fun remove(key: String)
}

@Serializable
internal data class StoredClientLog(
    val type: String,
    val clientVersion: String,
    val emittedAtMillis: Long,
    val message: Message,
    val structuredProperties: JsonObject? = null,
)

internal class ClientLogHistory(
    private val storage: ClientLogStorage = BrowserClientLogStorage,
    private val maxBytes: Int = MAX_BYTES,
    private val maxRecords: Int = MAX_RECORDS,
    private val maxAgeMillis: Long = MAX_AGE_MILLIS,
    private val maxRecordBytes: Int = MAX_RECORD_BYTES,
) {
    private var expectedEpoch = runCatching { storage.get(EPOCH_KEY) ?: "0" }.getOrDefault("0")
    private var recordSequence = 0L
    var droppedCount: Int = 0
        private set
    var expiredCount: Int = 0
        private set
    var evictedCount: Int = 0
        private set
    var status: String = "available"
        private set

    fun append(
        message: Message,
        clientVersion: String?,
        emittedAtMillis: Long,
        structuredProperties: JsonObject? = null,
    ): Boolean = safely {
        if ((storage.get(EPOCH_KEY) ?: "0") != expectedEpoch) {
            droppedCount += 1
            status = "cleared in another tab; reload to resume history"
            return@safely false
        }
        val record = StoredClientLog(
            type = "client-log-event",
            clientVersion = clientVersion?.takeIf(String::isNotBlank) ?: "unknown",
            emittedAtMillis = emittedAtMillis,
            message = message,
            structuredProperties = structuredProperties,
        )
        val serialized = Json.encodeToString(record)
        val serializedBytes = serialized.encodeToByteArray().size
        val sequence = recordSequence + 1
        val keyBytes = recordKey(emittedAtMillis, 0, sequence).encodeToByteArray().size
        val bytes = serializedBytes + keyBytes
        if (bytes > maxRecordBytes || bytes > maxBytes || maxRecords == 0) {
            droppedCount += 1
            return@safely false
        }

        val active = activeRecordKeys(emittedAtMillis)
        val evicted = evictFor(active, bytes)
        val key = recordKey(emittedAtMillis, bytes, sequence)
        recordSequence = sequence
        try {
            storage.put(key, serialized)
        } catch (failure: Throwable) {
            evicted.forEach { (oldKey, oldValue) ->
                runCatching { storage.put(oldKey, oldValue) }
            }
            throw failure
        }
        val additionallyEvicted = trimToCapacity(emittedAtMillis)
        if (storage.get(key) == null) {
            droppedCount += 1
            evictedCount += evicted.size + additionallyEvicted
            return@safely false
        }
        if ((storage.get(EPOCH_KEY) ?: "0") != expectedEpoch) {
            storage.remove(key)
            droppedCount += 1
            status = "cleared in another tab; reload to resume history"
            return@safely false
        }
        evictedCount += evicted.size + additionallyEvicted
        true
    } ?: false

    fun read(nowMillis: Long): List<StoredClientLog> = safely {
        storage.keys()
            .filter { it.startsWith(RECORD_PREFIX) }
            .sorted()
            .mapNotNull { key -> readRecord(key, nowMillis) }
    } ?: emptyList()

    fun clear() {
        safely {
            expectedEpoch = ((storage.get(EPOCH_KEY)?.toLongOrNull() ?: 0L) + 1L).toString()
            storage.put(EPOCH_KEY, expectedEpoch)
            storage.keys().filter { it.startsWith(RECORD_PREFIX) }.forEach(storage::remove)
            status = "available"
            true
        }
    }

    private fun readRecord(key: String, nowMillis: Long): StoredClientLog? {
        val emittedAtMillis = key.removePrefix(RECORD_PREFIX).substringBefore('.').toLongOrNull()
        val raw = storage.get(key) ?: return null
        val record = runCatching { Json.decodeFromString<StoredClientLog>(raw) }.getOrNull()
        if (emittedAtMillis == null || record == null) {
            droppedCount += 1
            storage.remove(key)
            return null
        }
        if (nowMillis - emittedAtMillis > maxAgeMillis) {
            expiredCount += 1
            storage.remove(key)
            return null
        }
        return record
    }

    private fun evictFor(records: List<RecordKey>, newRecordBytes: Int): List<Pair<String, String>> {
        var retainedBytes = records.sumOf { it.byteSize }
        var retainedCount = records.size
        val evicted = mutableListOf<Pair<String, String>>()
        records.forEach { record ->
            if (retainedBytes + newRecordBytes > maxBytes || retainedCount + 1 > maxRecords) {
                storage.get(record.key)?.let { evicted.add(record.key to it) }
                storage.remove(record.key)
                retainedBytes -= record.byteSize
                retainedCount -= 1
            }
        }
        return evicted
    }

    private fun trimToCapacity(nowMillis: Long): Int {
        val records = activeRecordKeys(nowMillis)
        var retainedBytes = records.sumOf { it.byteSize }
        var retainedCount = records.size
        var evicted = 0
        records.forEach { record ->
            if (retainedBytes > maxBytes || retainedCount > maxRecords) {
                storage.remove(record.key)
                retainedBytes -= record.byteSize
                retainedCount -= 1
                evicted += 1
            }
        }
        return evicted
    }

    private fun activeRecordKeys(nowMillis: Long): List<RecordKey> = storage.keys()
        .filter { it.startsWith(RECORD_PREFIX) }
        .sorted()
        .mapNotNull { key ->
            val parts = key.removePrefix(RECORD_PREFIX).split('.')
            val timestamp = parts.getOrNull(0)?.toLongOrNull()
            val byteSize = parts.getOrNull(2)?.toIntOrNull()
            if (timestamp == null || byteSize == null) {
                storage.remove(key)
                null
            } else if (nowMillis - timestamp > maxAgeMillis) {
                expiredCount += 1
                storage.remove(key)
                null
            } else {
                RecordKey(key, timestamp, byteSize)
            }
        }

    private fun recordKey(emittedAtMillis: Long, byteSize: Int, sequence: Long) = "$RECORD_PREFIX${emittedAtMillis.toString().padStart(13, '0')}." +
        "${sequence.toString().padStart(8, '0')}.${byteSize.toString().padStart(5, '0')}.${Uuid.random()}"

    private data class RecordKey(val key: String, val timestamp: Long, val byteSize: Int)

    private inline fun <T> safely(block: () -> T): T? = try {
        block()
    } catch (_: Throwable) {
        status = "browser storage unavailable"
        droppedCount += 1
        null
    }

    companion object {
        const val MAX_BYTES = 1_048_576
        const val MAX_RECORDS = 2_000
        const val MAX_RECORD_BYTES = 8_192
        const val MAX_AGE_MILLIS = 30L * 24L * 60L * 60L * 1_000L
        private const val RECORD_PREFIX = "coupling:client-log:record:"
        private const val EPOCH_KEY = "coupling:client-log:epoch"
    }
}

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
            if (name in safeActionFields) put(name, value.toJsonElement())
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

private object BrowserClientLogStorage : ClientLogStorage {
    override fun keys() = (0 until localStorage.length).mapNotNull(localStorage::key)
    override fun get(key: String) = localStorage.getItem(key)
    override fun put(key: String, value: String) = localStorage.setItem(key, value)
    override fun remove(key: String) = localStorage.removeItem(key)
}
