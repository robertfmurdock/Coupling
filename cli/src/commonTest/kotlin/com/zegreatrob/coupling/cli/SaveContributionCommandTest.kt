package com.zegreatrob.coupling.cli

import com.benasher44.uuid.uuid4
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.testing.test
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.SaveContributionCommandWrapper
import com.zegreatrob.coupling.cli.party.ContributionContext
import com.zegreatrob.coupling.cli.party.SaveContribution
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.coupling.testaction.StubCannon
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.tools.digger.json.toJsonString
import com.zegreatrob.tools.digger.model.Contribution
import kotlinx.datetime.Clock
import kotlin.test.Test

class SaveContributionCommandTest {

    @Test
    fun canOverrideFileFields() = asyncSetup(object : ScopeMint() {
        val sourceContribution = Contribution(
            lastCommit = "${uuid4()}",
            firstCommit = "${uuid4()}",
            authors = listOf("${uuid4()}"),
            dateTime = Clock.System.now(),
            ease = 7,
            storyId = "${uuid4()}",
            semver = "${uuid4()}",
            label = "${uuid4()}",
            firstCommitDateTime = Clock.System.now(),
        )
        val partyId = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also { it.givenAny(SaveContributionCommandWrapper::class, VoidResult.Accepted) }
        val command = SaveContribution(exerciseScope, cannon)
            .context { obj = ContributionContext(partyId, "local") }
        val labelOverride = uuidString()
        val linkOverride = uuidString()
    }) exercise {
        command.test("--label $labelOverride --link $linkOverride --input-json \'${sourceContribution.toJsonString()}\'")
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.stderr)
        receivedActions.firstOrNull()
            ?.let { it as? SaveContributionCommand }
            .assertIsEqualTo(
                SaveContributionCommand(
                    partyId = partyId,
                    contributionId = sourceContribution.firstCommit,
                    participantEmails = sourceContribution.authors.toSet(),
                    hash = sourceContribution.lastCommit,
                    dateTime = sourceContribution.dateTime,
                    ease = sourceContribution.ease,
                    story = sourceContribution.storyId,
                    link = linkOverride,
                    semver = sourceContribution.semver,
                    label = labelOverride,
                    firstCommit = sourceContribution.firstCommit,
                ),
            )
    }
}
