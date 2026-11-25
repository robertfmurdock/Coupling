package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.LoggingActionPipe
import com.zegreatrob.coupling.client.routing.authenticatedRoutes
import com.zegreatrob.coupling.logging.Message
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.action.async.SuspendAction
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import io.github.oshai.kotlinlogging.KLoggingEvent
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level
import kotlinx.browser.document
import kotlinx.coroutines.await
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import react.create
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.uuid.Uuid

class ClientLogHistoryTest {

    @AfterTest
    fun cleanUp() {
        ClientLogCapture.uninstall()
        ClientLogCapture.clear()
        cleanup()
    }

    @Test
    fun storedEventsKeepTheirEmissionVersionAndStructuredFields() = setup(object {
        val storage = MemoryClientLogStorage()
        val history = ClientLogHistory(storage, maxBytes = 20_000, maxRecords = 20)
        val start = actionEvent("start", "a-random-trace")
        val end = actionEvent("end", "a-random-trace")
        val typedProperties = buildJsonObject {
            put("command_duration_ms", 12.345)
            put("command_error", false)
        }
    }) exercise {
        history.append(start, "2.4.1", emittedAtMillis = 1_000)
        history.append(end, "2.5.0", emittedAtMillis = 2_000, structuredProperties = typedProperties)
        history.read(nowMillis = 2_000)
    } verify { records ->
        records.map { it.clientVersion }.assertIsEqualTo(listOf("2.4.1", "2.5.0"))
        records.map { it.message.properties?.get("command_phase") }.assertIsEqualTo(listOf("start", "end"))
        records.map { it.message.properties?.get("command_trace_id") }
            .assertIsEqualTo(listOf("a-random-trace", "a-random-trace"))
        records.last().structuredProperties?.get("command_duration_ms").toString().assertIsEqualTo("12.345")
        records.last().structuredProperties?.get("command_error").toString().assertIsEqualTo("false")
        history.read(nowMillis = 2_000).size.assertIsEqualTo(2)
        storage.get(storage.keys().first())?.contains("\"clientVersion\":\"2.4.1\"").assertIsEqualTo(true)
    }

    @Test
    fun expiredEventsAreRemovedAndNotReturned() = setup(object {
        val storage = MemoryClientLogStorage()
        val history = ClientLogHistory(storage, maxAgeMillis = 30_000)
    }) exercise {
        history.append(actionEvent("end", "trace"), "2.4.1", emittedAtMillis = 1_000)
        history.read(nowMillis = 31_001)
    } verify { records ->
        records.assertIsEqualTo(emptyList())
        storage.size.assertIsEqualTo(0)
    }

    @Test
    fun capacityEvictsOldestEventsWithoutRemovingUnrelatedStorage() = setup(object {
        val storage = MemoryClientLogStorage().apply { put("other-app-value", "keep") }
        val history = ClientLogHistory(storage, maxBytes = 800, maxRecords = 20)
    }) exercise {
        history.append(actionEvent("end", "first"), "2.4.1", emittedAtMillis = 1_000)
        history.append(actionEvent("end", "second"), "2.4.1", emittedAtMillis = 2_000)
        history.append(actionEvent("end", "third"), "2.4.1", emittedAtMillis = 3_000)
        history.read(nowMillis = 3_000)
    } verify { records ->
        records.map { it.message.properties?.get("command_trace_id") }.assertIsEqualTo(listOf("third"))
        storage.get("other-app-value").assertIsEqualTo("keep")
    }

    @Test
    fun missingBuildVersionIsExportedAsUnknown() = setup(object {
        val history = ClientLogHistory(MemoryClientLogStorage())
    }) exercise {
        history.append(actionEvent("end", "trace"), null, emittedAtMillis = 1_000)
        history.read(nowMillis = 1_000).single().clientVersion
    } verify { version ->
        version.assertIsEqualTo("unknown")
    }

    @Test
    fun oversizedEventsAreDroppedAndReported() = setup(object {
        val history = ClientLogHistory(MemoryClientLogStorage(), maxRecordBytes = 200)
    }) exercise {
        history.append(actionEvent("end", "trace"), "2.4.1", emittedAtMillis = 1_000)
    } verify { stored ->
        stored.assertIsEqualTo(false)
        history.read(nowMillis = 1_000).assertIsEqualTo(emptyList())
        history.droppedCount.assertIsEqualTo(1)
    }

    @Test
    fun aClearInOneTabPreventsAnOlderTabFromRepopulatingHistory() = setup(object {
        val storage = MemoryClientLogStorage()
        val firstTab = ClientLogHistory(storage)
        val secondTab = ClientLogHistory(storage)
    }) exercise {
        firstTab.append(actionEvent("end", "before-clear"), "2.4.1", emittedAtMillis = 1_000)
        secondTab.clear()
        val staleWrite = firstTab.append(actionEvent("end", "stale-tab"), "2.4.1", emittedAtMillis = 2_000)
        secondTab.append(actionEvent("end", "after-clear"), "2.4.1", emittedAtMillis = 3_000)
        staleWrite to secondTab.read(nowMillis = 3_000)
    } verify { (staleWrite, records) ->
        staleWrite.assertIsEqualTo(false)
        records.map { it.message.properties?.get("command_trace_id") }.assertIsEqualTo(listOf("after-clear"))
    }

    @Test
    fun clearDuringAWriteRemovesTheStaleRecordBeforeItCanBeRecovered() = setup(object {
        val storage = MemoryClientLogStorage()
        val writingTab = ClientLogHistory(storage)
        val clearingTab = ClientLogHistory(storage)
    }) exercise {
        storage.onRecordWrite = clearingTab::clear
        val stored = writingTab.append(actionEvent("end", "stale-write"), "2.4.1", emittedAtMillis = 1_000)
        stored to clearingTab.read(nowMillis = 1_000)
    } verify { (stored, records) ->
        stored.assertIsEqualTo(false)
        records.assertIsEqualTo(emptyList())
    }

    @Test
    fun interleavedTabsAppendToTheSharedHistoryWithinTheConfiguredCap() = setup(object {
        val storage = MemoryClientLogStorage()
        val firstTab = ClientLogHistory(storage, maxRecords = 3)
        val secondTab = ClientLogHistory(storage, maxRecords = 3)
    }) exercise {
        firstTab.append(actionEvent("end", "first-tab-1"), "2.4.1", emittedAtMillis = 1_000)
        secondTab.append(actionEvent("end", "second-tab"), "2.5.0", emittedAtMillis = 2_000)
        firstTab.append(actionEvent("end", "first-tab-2"), "2.4.1", emittedAtMillis = 3_000)
        secondTab.append(actionEvent("end", "second-tab-2"), "2.5.0", emittedAtMillis = 4_000)
        secondTab.read(nowMillis = 4_000)
    } verify { records ->
        records.map { it.message.properties?.get("command_trace_id") }
            .assertIsEqualTo(listOf("second-tab", "first-tab-2", "second-tab-2"))
        records.size.assertIsEqualTo(3)
    }

    @Test
    fun storageWriteFailureDoesNotEscapeIntoTheApplication() = setup(object {
        val storage = MemoryClientLogStorage().apply { failWrites = true }
        val history = ClientLogHistory(storage)
    }) exercise {
        history.append(actionEvent("end", "trace"), "2.4.1", emittedAtMillis = 1_000)
    } verify { stored ->
        stored.assertIsEqualTo(false)
        history.status.assertIsEqualTo("browser storage unavailable")
        history.droppedCount.assertIsEqualTo(1)
    }

    @Test
    fun failedWriteRestoresRecordsEvictedForCapacity() = setup(object {
        val storage = MemoryClientLogStorage()
        val history = ClientLogHistory(storage, maxBytes = 800, maxRecords = 20)
    }) exercise {
        history.append(actionEvent("end", "retained"), "2.4.1", emittedAtMillis = 1_000)
        storage.failNextWrite = true
        val stored = history.append(actionEvent("end", "rejected"), "2.4.1", emittedAtMillis = 2_000)
        stored to history.read(nowMillis = 2_000)
    } verify { (stored, records) ->
        stored.assertIsEqualTo(false)
        records.map { it.message.properties?.get("command_trace_id") }.assertIsEqualTo(listOf("retained"))
    }

    @Test
    fun corruptRecordsAreRemovedAndCountedAsDropped() = setup(object {
        val storage = MemoryClientLogStorage().apply {
            put("coupling:client-log:record:0000000001000.00000001.00010.broken", "not-json")
        }
        val history = ClientLogHistory(storage)
    }) exercise {
        history.read(nowMillis = 1_000)
    } verify { records ->
        records.assertIsEqualTo(emptyList())
        storage.size.assertIsEqualTo(0)
        history.droppedCount.assertIsEqualTo(1)
    }

    @Test
    fun browserAppenderCapturesRealActionEventsWithTheEmissionVersion() = asyncSetup(object {
        val pipe = LoggingActionPipe(Uuid.random())
    }) exercise {
        ClientLogCapture.clear()
        ClientLogCapture.install("2.5.0")
        KotlinLoggingConfiguration.direct.appender.log(
            KLoggingEvent(
                level = Level.INFO,
                marker = null,
                loggerName = "ActionLogger",
                message = "private_token=never-persist-this",
                payload = mapOf(
                    "command_action" to "SyntheticAction",
                    "private_token" to "never-persist-this",
                ),
            ),
        )
        pipe.execute(Unit, ClientHistoryTestAction)
        ClientLogCapture.export() to ClientLogHistory().read(kotlin.js.Date.now().toLong())
    } verify { (jsonl, reopenedHistory) ->
        jsonl.contains("\"clientVersion\":\"2.5.0\"").assertIsEqualTo(true)
        jsonl.contains("\"command_action\":\"ClientHistoryTestAction\"").assertIsEqualTo(true)
        jsonl.contains("\"command_duration_ms\":").assertIsEqualTo(true)
        jsonl.contains("\"command_trace_id\":").assertIsEqualTo(true)
        jsonl.contains("never-persist-this").assertIsEqualTo(false)
        jsonl.contains("private_token").assertIsEqualTo(false)
        reopenedHistory.size.assertIsEqualTo(3)
        reopenedHistory.map { it.clientVersion }.assertIsEqualTo(listOf("2.5.0", "2.5.0", "2.5.0"))
        ClientLogCapture.clear()
    }

    @Test
    fun recoveryControlsHaveClearAccessibleActions() = asyncSetup {
        render(ClientLogHistoryControls.create())
    } exercise {
        listOf(
            screen.getByRole("button", RoleOptions(name = "Download client logs (JSONL)")).textContent,
            screen.getByRole("button", RoleOptions(name = "Clear saved client logs")).textContent,
        )
    } verify { labels ->
        labels.assertIsEqualTo(listOf("Download client logs (JSONL)", "Clear saved client logs"))
    }

    @Test
    fun aboutArticleDoesNotIncludeDeveloperRecoveryControls() = asyncSetup {
        renderNavigation { AboutPage() }
    } exercise {
        waitFor { screen.getByRole("heading", RoleOptions(name = "About This App")) }.await()
        document.body?.textContent?.contains("Client log history")
    } verify { recoveryControlsInterruptArticle ->
        recoveryControlsInterruptArticle.assertIsEqualTo(false)
    }

    @Test
    fun developerSettingsIsRegisteredAsAnAuthenticatedPartyRoute() = setup(object {
        val config = ClientConfig(false, "test", "example.invalid", "", "test", "", "", "unknown")
    }) exercise {
        config.authenticatedRoutes().mapNotNull { it.path }.filter { it.endsWith("developer-settings/") }
    } verify { routes ->
        routes.assertIsEqualTo(listOf("/:partyId/developer-settings/"))
    }

    @Test
    fun developerSettingsShowsRecoveryActionsAndClearUpdatesStatus() = asyncSetup(object {
        val actor = userEvent.setup()
    }) {
        ClientLogCapture.clear()
        ClientLogHistory().append(actionEvent("end", "developer-settings-test"), "2.5.0", kotlin.js.Date.now().toLong())
        renderNavigation { DeveloperSettingsPage() }
    } exercise {
        waitFor { screen.getByRole("heading", RoleOptions(name = "Developer settings")) }.await()
        actor.click(screen.getByText("Querying the exported JSONL")).await()
        actor.click(screen.getByRole("button", RoleOptions(name = "Clear saved client logs"))).await()
        listOf(
            screen.getByRole("heading", RoleOptions(name = "Developer settings")).textContent,
            screen.getByRole("heading", RoleOptions(name = "Client log history")).textContent,
            screen.getByRole("link", RoleOptions(name = "Back to parties")).getAttribute("href"),
            screen.getByRole("button", RoleOptions(name = "Download client logs (JSONL)")).textContent,
            screen.getByRole("button", RoleOptions(name = "Clear saved client logs")).textContent,
            screen.getByText("Each line is JSON. The first line is export coverage metadata; event lines can be filtered with jq:").textContent,
            screen.getByRole("status", RoleOptions()).textContent,
        )
    } verify { renderedPage ->
        renderedPage.assertIsEqualTo(
            listOf(
                "Developer settings",
                "Client log history",
                "/parties/",
                "Download client logs (JSONL)",
                "Clear saved client logs",
                "Each line is JSON. The first line is export coverage metadata; event lines can be filtered with jq:",
                "0 saved client log events. Storage: available. History expires after 30 days and may be shortened by the 1 MiB storage cap. Exported logs stay on this device until you choose to share them.",
            ),
        )
    }
}

private data object ClientHistoryTestAction : SuspendAction<Unit, Unit> {
    override suspend fun execute(dispatcher: Unit) = Unit
}

private fun actionEvent(phase: String, traceId: String) = Message(
    level = "INFO",
    name = "ActionLogger",
    message = "action $phase",
    properties = mapOf(
        "command_action" to "LoadPartyAction",
        "command_phase" to phase,
        "command_trace_id" to traceId,
        "command_duration_ms" to if (phase == "end") "12.345" else null,
        "command_error" to "false",
    ),
    timestamp = "2026-11-24T12:00:00Z",
)

private class MemoryClientLogStorage : ClientLogStorage {
    private val values = linkedMapOf<String, String>()
    val size get() = values.size
    var failWrites = false
    var failNextWrite = false
    var onRecordWrite: (() -> Unit)? = null

    override fun keys() = values.keys.toList()
    override fun get(key: String) = values[key]
    override fun put(key: String, value: String) {
        if (failWrites) error("storage blocked")
        if (failNextWrite) {
            failNextWrite = false
            error("quota exceeded")
        }
        if (key.startsWith("coupling:client-log:record:")) onRecordWrite?.also { onRecordWrite = null }?.invoke()
        values[key] = value
    }
    override fun remove(key: String) {
        values.remove(key)
    }
}
