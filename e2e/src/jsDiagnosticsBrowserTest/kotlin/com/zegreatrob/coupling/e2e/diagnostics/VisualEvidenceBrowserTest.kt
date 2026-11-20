package com.zegreatrob.coupling.e2e.diagnostics

import com.zegreatrob.coupling.e2e.visual.CAPTURE_FIXTURE
import com.zegreatrob.coupling.e2e.visual.CaptureRequest
import com.zegreatrob.coupling.e2e.visual.Dimensions
import com.zegreatrob.coupling.e2e.visual.EvidenceWdio
import com.zegreatrob.coupling.e2e.visual.SourceIdentity
import com.zegreatrob.coupling.e2e.visual.WdioEvidenceBrowser
import com.zegreatrob.coupling.e2e.visual.beginEvidence
import com.zegreatrob.coupling.e2e.visual.captureEvidence
import com.zegreatrob.coupling.e2e.visual.finishEvidence
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.await
import kotlin.test.Test

class VisualEvidenceBrowserTest {
    @Test
    fun realChromeCapturesExactNarrowViewportAndPngDimensions() = asyncSetup(BrowserFixture()) {
        open()
        waitFor("return window.fixtureFinished === true")
    } exercise {
        val request = CaptureRequest(CAPTURE_FIXTURE, "Browser integration", "390x844")
        val initial = beginEvidence(directory, SourceIdentity("browser-integration", false), request)
        val captured = captureEvidence(WdioEvidenceBrowser(browser.unsafeCast<EvidenceWdio>()), request, initial) {}
        finishEvidence(captured.paths.manifest, 0)
    } verifyAnd { report ->
        report.status.assertIsEqualTo("complete")
        report.actualViewport.assertIsEqualTo(Dimensions(390, 844))
        report.imageDimensions.assertIsEqualTo(Dimensions(390, 844))
        report.deviceScale.assertIsEqualTo(1.0)
        (Files.stat(report.paths.screenshot).await().size > 0).assertIsEqualTo(true)
    } teardown { close() }
}
