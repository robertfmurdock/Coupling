package com.zegreatrob.coupling.e2e.visual

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.await
import kotlin.test.Test

class CaptureEvidenceTest {
    @Test
    fun captureRecordsMeasuredEvidenceWithoutCompletingTheRun() = asyncSetup(World()) {
        open()
    } exercise {
        captureEvidence(browser, request, initial) {}
    } verifyAnd { report ->
        report.status.assertIsEqualTo("captured")
        report.actualViewport.assertIsEqualTo(Dimensions(390, 844))
        report.imageDimensions.assertIsEqualTo(Dimensions(390, 844))
        report.requestedViewport.assertIsEqualTo(report.actualViewport)
        report.deviceScale.assertIsEqualTo(1.0)
        report.theme.assertIsEqualTo("light")
        report.browser.assertIsEqualTo(browser.identity)
        report.source.assertIsEqualTo(source)
        report.fixture.assertIsEqualTo(FixtureIdentity(CAPTURE_FIXTURE))
        report.label.assertIsEqualTo("Before")
        readManifest(report.paths.manifest).assertIsEqualTo(report)
    } teardown { close() }

    @Test
    fun successfulRunnerCompletesCapturedEvidence() = asyncSetup(World()) {
        open()
        captureEvidence(browser, request, initial) {}
    } exercise {
        finishEvidence(initial.paths.manifest, 0)
    } verifyAnd { report ->
        report.status.assertIsEqualTo("complete")
        readManifest(initial.paths.manifest).assertIsEqualTo(report)
    } teardown { close() }

    @Test
    fun developerSettingsIsARepeatableVisualEvidenceFixture() = asyncSetup(World()) {
        request = request.copy(fixture = DEVELOPER_SETTINGS_FIXTURE)
        open()
    } exercise {
        captureEvidence(browser, request, initial) {}
    } verifyAnd { report ->
        report.fixture.assertIsEqualTo(FixtureIdentity(DEVELOPER_SETTINGS_FIXTURE, DEVELOPER_SETTINGS_SEED))
        report.status.assertIsEqualTo("captured")
        browser.photographed.assertIsEqualTo(true)
        report.fixtureConditions.assertIsEqualTo(
            listOf(
                "Authenticated E2E test account; app-owned client-log storage cleared before visiting Developer settings",
                "Developer settings heading framed with client log history controls; animations and external avatars disabled",
                "Browser locale en-US, timezone UTC, light color scheme, device scale 1",
            ),
        )
    } teardown { close() }

    @Test
    fun unknownFixtureFailsBeforePreparingData() = asyncSetup(World()) {
        request = request.copy(fixture = "unknown")
        open()
    } exercise {
        runCatching { captureEvidence(browser, request, initial) { prepared = true } }
    } verifyAnd { result ->
        result.exceptionOrNull()?.message.assertIsEqualTo("Unknown fixture")
        prepared.assertIsEqualTo(false)
        readManifest(initial.paths.manifest).status.assertIsEqualTo("failed")
    } teardown { close() }

    @Test
    fun unsupportedViewportRecordsActualSizeWithoutPhotographing() = asyncSetup(World()) {
        open()
        browser.measured = Dimensions(500, 844)
    } exercise {
        runCatching { captureEvidence(browser, request, initial) {} }
    } verifyAnd { result ->
        result.exceptionOrNull()?.message.assertIsEqualTo("Unsupported viewport")
        browser.photographed.assertIsEqualTo(false)
        val report = readManifest(initial.paths.manifest)
        report.status.assertIsEqualTo("failed")
        report.actualViewport.assertIsEqualTo(browser.measured)
    } teardown { close() }

    @Test
    fun partialPreparationIsVisibleAndFailureExcludesAuthPayload() = asyncSetup(object {
        val world = World()
        val preparationFailure = IllegalStateException("Bearer secret-token password=secret")
        var statusDuringPreparation: String? = null
    }) {
        world.open()
    } exercise {
        runCatching {
            captureEvidence(world.browser, world.request, world.initial) {
                statusDuringPreparation = readManifest(world.initial.paths.manifest).status
                throw preparationFailure
            }
        }
    } verifyAnd { result ->
        statusDuringPreparation.assertIsEqualTo("partial")
        result.exceptionOrNull().assertIsEqualTo(preparationFailure)
        val report = readManifest(world.initial.paths.manifest)
        report.status.assertIsEqualTo("failed")
        report.failureStage.assertIsEqualTo("fixture")
        CaptureFiles.readFile(world.initial.paths.manifest, "utf8").await().contains("secret").assertIsEqualTo(false)
    } teardown { world.close() }

    @Test
    fun partialRunCannotCompleteEvenWhenRunnerExitsSuccessfully() = asyncSetup(World()) {
        open()
    } exercise {
        finishEvidence(initial.paths.manifest, 0)
    } verifyAnd { report ->
        report.status.assertIsEqualTo("failed")
    } teardown { close() }

    @Test
    fun runnerFailureAfterScreenshotCannotComplete() = asyncSetup(World()) {
        open()
        captureEvidence(browser, request, initial) {}
    } exercise {
        finishEvidence(initial.paths.manifest, 1)
    } verifyAnd { report ->
        report.status.assertIsEqualTo("failed")
    } teardown { close() }

    @Test
    fun imageSizeMismatchCannotReportSuccessfulEvidence() = asyncSetup(World()) {
        open()
        browser.imageDimensions = Dimensions(500, 844)
    } exercise {
        runCatching { captureEvidence(browser, request, initial) {} }
    } verifyAnd { result ->
        result.exceptionOrNull()?.message.assertIsEqualTo("Image dimensions do not match measured viewport and device scale")
        readManifest(initial.paths.manifest).status.assertIsEqualTo("failed")
    } teardown { close() }

    @Test
    fun repeatedLabelsPreserveBothRuns() = asyncSetup(World()) {
        open()
    } exercise {
        beginEvidence(directory, source, request)
    } verifyAnd { second ->
        (initial.paths.manifest != second.paths.manifest).assertIsEqualTo(true)
        readManifest(initial.paths.manifest).label.assertIsEqualTo(second.label)
    } teardown { close() }
}

private class World {
    lateinit var directory: String
    lateinit var initial: EvidenceManifest
    val source = SourceIdentity("abc123", true)
    var request = CaptureRequest(CAPTURE_FIXTURE, "Before", "390x844")
    val browser = BrowserDouble()
    var prepared = false

    suspend fun open() {
        directory = CaptureFiles.mkdtemp("/tmp/coupling-evidence-").await()
        try {
            initial = beginEvidence(directory, source, request)
        } catch (failure: Throwable) {
            runCatching { close() }.onFailure(failure::addSuppressed)
            throw failure
        }
    }

    suspend fun close() {
        CaptureFiles.rm(directory, RemoveOptions(recursive = true, force = true)).await()
    }
}

private class BrowserDouble : EvidenceBrowser {
    override val identity = BrowserIdentity("chrome", "123")
    var measured = Dimensions(390, 844)
    var imageDimensions = measured
    var photographed = false
    override suspend fun setViewport(dimensions: Dimensions) = Unit
    override suspend fun measure() = ViewportMeasurement(measured, 1.0, "light")
    override suspend fun screenshot() = CapturedImage("", imageDimensions).also { photographed = true }
}
