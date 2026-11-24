package com.zegreatrob.coupling.e2e.visual

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.js.Date
import kotlin.js.Promise

const val CAPTURE_FIXTURE = "contribution-overview"
const val CAPTURE_SEED = "contribution-overview-v1"
const val DEVELOPER_SETTINGS_FIXTURE = "developer-settings"
const val DEVELOPER_SETTINGS_SEED = "developer-settings-v1"
const val CAPTURE_SAVE_TIMESTAMP = "12:00:00, 2026-11-19"

private val fixtureSeeds = mapOf(
    CAPTURE_FIXTURE to CAPTURE_SEED,
    DEVELOPER_SETTINGS_FIXTURE to DEVELOPER_SETTINGS_SEED,
)

@Serializable
data class Dimensions(val width: Int, val height: Int)

@Serializable
data class SourceIdentity(val revision: String, val workingTreeDirty: Boolean)

@Serializable
data class FixtureIdentity(val name: String, val seed: String = CAPTURE_SEED)

@Serializable
data class BrowserIdentity(val name: String, val version: String)

@Serializable
data class EvidencePaths(val manifest: String, val screenshot: String)

@Serializable
data class EvidenceManifest(
    val source: SourceIdentity,
    val fixture: FixtureIdentity,
    val label: String,
    val paths: EvidencePaths,
    val requestedViewportInput: String,
    val schemaVersion: Int = 1,
    val timestamp: String = Date().toISOString(),
    val status: String = "partial",
    val theme: String = "light",
    val requestedViewport: Dimensions? = null,
    val actualViewport: Dimensions? = null,
    val imageDimensions: Dimensions? = null,
    val deviceScale: Double? = null,
    val browser: BrowserIdentity? = null,
    val failureStage: String? = null,
    val fixtureConditions: List<String> = emptyList(),
)

data class CaptureRequest(val fixture: String, val label: String, val viewport: String)
data class ViewportMeasurement(val dimensions: Dimensions, val deviceScale: Double, val theme: String)
data class CapturedImage(val base64: String, val dimensions: Dimensions)

interface EvidenceBrowser {
    val identity: BrowserIdentity
    suspend fun setViewport(dimensions: Dimensions)
    suspend fun measure(): ViewportMeasurement
    suspend fun screenshot(): CapturedImage
}

private val manifestJson = Json {
    prettyPrint = true
    encodeDefaults = true
}

suspend fun readManifest(path: String): EvidenceManifest = manifestJson.decodeFromString(CaptureFiles.readFile(path, "utf8").await())

suspend fun saveManifest(manifest: EvidenceManifest) {
    val path = manifest.paths.manifest
    CaptureFiles.writeFile("$path.tmp", manifestJson.encodeToString(manifest) + "\n").await()
    CaptureFiles.rename("$path.tmp", path).await()
}

suspend fun beginEvidence(directory: String, source: SourceIdentity, request: CaptureRequest): EvidenceManifest {
    val runDirectory = CapturePath.join(directory, CaptureCrypto.randomUUID())
    CaptureFiles.mkdir(runDirectory, DirectoryOptions(recursive = true)).await()
    return EvidenceManifest(
        source,
        FixtureIdentity(request.fixture, fixtureSeeds[request.fixture] ?: "unknown"),
        request.label,
        EvidencePaths(CapturePath.join(runDirectory, "manifest.json"), CapturePath.join(runDirectory, "capture.png")),
        requestedViewportInput = request.viewport,
        requestedViewport = runCatching { parseViewport(request.viewport) }.getOrNull(),
        fixtureConditions = fixtureConditions(request.fixture),
    )
        .also { saveManifest(it) }
}

private fun parseViewport(viewport: String): Dimensions {
    val match = Regex("^(\\d+)x(\\d+)$").matchEntire(viewport)
    val values = match?.groupValues?.drop(1)?.map { it.toIntOrNull() }
    require(values != null && values.all { it != null && it in 1..8192 }) { "Invalid viewport; use WIDTHxHEIGHT from 1 to 8192 CSS pixels" }
    return Dimensions(values[0]!!, values[1]!!)
}

suspend fun captureEvidence(
    browser: EvidenceBrowser,
    request: CaptureRequest,
    initial: EvidenceManifest,
    prepare: suspend () -> Unit,
): EvidenceManifest {
    var report = initial
    var stage = "validation"
    try {
        require(request.fixture in fixtureSeeds) { "Unknown fixture" }
        require(request.label.isNotBlank() && request.label.length <= 120) { "Invalid capture label" }
        report = report.copy(requestedViewport = parseViewport(request.viewport))
        saveManifest(report)
        stage = "viewport"
        browser.setViewport(report.requestedViewport!!)
        stage = "fixture"
        prepare()
        stage = "measurement"
        val measured = browser.measure()
        report = report.copy(
            actualViewport = measured.dimensions,
            deviceScale = measured.deviceScale,
            theme = measured.theme,
            browser = browser.identity,
        )
        check(report.actualViewport == report.requestedViewport) { "Unsupported viewport" }
        check(report.theme == "light") { "Unsupported theme" }
        stage = "screenshot"
        val image = browser.screenshot()
        report = report.copy(imageDimensions = image.dimensions)
        val expected = Dimensions(
            (measured.dimensions.width * measured.deviceScale).toInt(),
            (measured.dimensions.height * measured.deviceScale).toInt(),
        )
        check(image.dimensions == expected) { "Image dimensions do not match measured viewport and device scale" }
        CaptureFiles.writeFile(report.paths.screenshot, image.base64, "base64").await()
        return report.copy(status = "captured").also { saveManifest(it) }
    } catch (failure: Throwable) {
        // Authentication failures may contain credentials. Only persist the stage.
        saveManifest(report.copy(status = "failed", failureStage = stage))
        throw failure
    }
}

suspend fun finishEvidence(path: String, exitCode: Int): EvidenceManifest {
    val report = readManifest(path)
    val complete = exitCode == 0 && report.status == "captured"
    return report.copy(
        status = if (complete) "complete" else "failed",
        failureStage = if (complete) null else report.failureStage ?: "runner",
    )
        .also { saveManifest(it) }
}

fun captureRequest() = CaptureRequest(captureEnv("COUPLING_CAPTURE_FIXTURE"), captureEnv("COUPLING_CAPTURE_LABEL"), captureEnv("COUPLING_CAPTURE_VIEWPORT"))

private fun fixtureConditions(fixture: String) = when (fixture) {
    CAPTURE_FIXTURE -> listOf(
        "Fixed contribution IDs, dates, authors and cycle times; animations and external avatars disabled",
        "Save Timestamp display normalized to $CAPTURE_SAVE_TIMESTAMP; server records remain unchanged",
        "Browser locale en-US, timezone UTC, light color scheme, device scale 1",
    )

    DEVELOPER_SETTINGS_FIXTURE -> listOf(
        "Authenticated E2E test account; app-owned client-log storage cleared before visiting Developer settings",
        "Developer settings heading framed with client log history controls; animations and external avatars disabled",
        "Browser locale en-US, timezone UTC, light color scheme, device scale 1",
    )

    else -> emptyList()
}

internal object CaptureLifecycle {
    fun begin(): Promise<String> = MainScope().promise {
        val root = captureEnv("COUPLING_REPOSITORY_ROOT")
        fun git(vararg args: String) = CaptureChildProcess.execFileSync("git", args, GitOptions(cwd = root, encoding = "utf8")).trim()
        beginEvidence(
            captureEnv("COUPLING_CAPTURE_DIRECTORY"),
            SourceIdentity(git("rev-parse", "HEAD"), git("status", "--porcelain").isNotEmpty()),
            captureRequest(),
        ).paths.manifest
    }

    fun finish(path: String, exitCode: Int): Promise<Boolean> = MainScope().promise {
        finishEvidence(path, exitCode).status == "complete"
    }
}
