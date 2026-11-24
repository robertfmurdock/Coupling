package com.zegreatrob.coupling.e2e.visual

import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.e2e.test.ContributionOverviewPage
import com.zegreatrob.coupling.e2e.test.TestLogin
import com.zegreatrob.coupling.e2e.test.sdk
import com.zegreatrob.coupling.model.ContributionId
import com.zegreatrob.coupling.model.ContributionInput
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level
import kotlinx.coroutines.await
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class CaptureFixture {
    @Test
    fun captureNamedFixture() = asyncSetup(object {
        val request = captureRequest()
        val manifest = captureEnv("COUPLING_CAPTURE_MANIFEST")
        var fixtureStarted = false
    }) {
        KotlinLoggingConfiguration.direct.logLevel = Level.WARN
    } exercise {
        try {
            captureEvidence(WdioEvidenceBrowser(), request, readManifest(manifest)) {
                when (request.fixture) {
                    DEVELOPER_SETTINGS_FIXTURE -> prepareDeveloperSettings()

                    CAPTURE_FIXTURE -> {
                        fixtureStarted = true
                        prepareContributionOverview()
                    }
                }
            }
        } finally {
            // Only this reserved fixture is deleted; the normal E2E bootstrap deletes all parties.
            if (fixtureStarted) sdk().fire(DeletePartyCommand(fixtureParty.id))
        }
    } verify { report ->
        check(report.status == "captured")
    }
}

private suspend fun prepareDeveloperSettings() {
    WebdriverBrowser.setUrl("https://localhost/")
    CaptureWdio.execute(
        """
        sessionStorage.setItem('animationDisabled', 'true');
        sessionStorage.setItem('thirdPartyAvatarsDisabled', 'true');
        """.trimIndent(),
    ).await()
    TestLogin.login {
        WebdriverBrowser.setLocation("/visual-evidence-client-logs/developer-settings/")
        WebdriverBrowser.waitUntil({ TestingLibraryBrowser.getByText("Developer settings").isDisplayed() }, timeout = 30000, timeoutMessage = "Developer settings did not become ready")
    }
    CaptureWdio.execute(
        """
        Object.keys(localStorage)
            .filter(key => key.startsWith('coupling:client-log:'))
            .forEach(key => localStorage.removeItem(key));
        """.trimIndent(),
    ).await()
    awaitVisualReadiness()
}

private val fixtureParty = PartyDetails(
    id = PartyId("visual-evidence-contribution-overview-v1"),
    name = "Contribution overview example",
    animationEnabled = false,
)

private suspend fun prepareContributionOverview() {
    val sdk = sdk()
    sdk.fire(DeletePartyCommand(fixtureParty.id))
    sdk.fire(SavePartyCommand(fixtureParty))
    sdk.fire(SaveContributionCommand(fixtureParty.id, fixtureContributions()))
    WebdriverBrowser.setUrl("https://localhost/")
    CaptureWdio.execute(
        """
        sessionStorage.setItem('animationDisabled', 'true');
        sessionStorage.setItem('thirdPartyAvatarsDisabled', 'true');
        """.trimIndent(),
    ).await()
    TestLogin.login()
    ContributionOverviewPage.goTo(fixtureParty.id)
    WebdriverBrowser.waitUntil({
        ContributionOverviewPage.summaryValue("Recent contributions") == "5" &&
            ContributionOverviewPage.summaryValue("Median cycle time") == "20m" &&
            ContributionOverviewPage.summaryValue("Contributors") == "2"
    }, timeout = 30000, timeoutMessage = "Contribution evidence fixture did not become ready")
    normalizeSaveTimestamps()
    awaitVisualReadiness()
}

private suspend fun normalizeSaveTimestamps() {
    val changed = CaptureWdio.execute(
        """
        const labels = Array.from(document.querySelectorAll('div'))
            .filter(element => element.textContent === 'Save Timestamp:');
        if (labels.length !== 5 || labels.some(label => !label.nextElementSibling)) {
            throw new Error('Expected five contribution Save Timestamp values');
        }
        labels.forEach(label => label.nextElementSibling.replaceChildren(arguments[0]));
        return labels.length;
        """.trimIndent(),
        CAPTURE_SAVE_TIMESTAMP,
    ).await()
    check(changed == 5) { "Save Timestamp normalization did not cover five contributions" }
}

@OptIn(ExperimentalKotoolsTypesApi::class)
private fun fixtureContributions() = (1..5).map { index ->
    ContributionInput(
        contributionId = ContributionId("visual-evidence-$index".toNotBlankString().getOrThrow()),
        name = "Example change $index",
        participantEmails = if (index == 1) setOf("alex@example.invalid", "sam@example.invalid") else setOf("alex@example.invalid"),
        dateTime = Instant.parse("2026-11-19T12:0$index:00Z"),
        integrationDateTime = Instant.parse("2026-11-19T12:0$index:00Z"),
        cycleTime = if (index <= 3) (index * 10).minutes else null,
        commitCount = index,
    )
}

private suspend fun awaitVisualReadiness() {
    CaptureWdio.execute(
        """
        return Promise.all([
            document.fonts.ready,
            ...Array.from(document.images).map(image => image.decode())
        ]).then(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
        """.trimIndent(),
    ).await()
}
