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
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

class SaveContributionCLITest {

    @Test
    fun canOverrideFields() = asyncSetup(object : ScopeMint() {
        val sourceContribution = Contribution(
            lastCommit = "${uuid4()}",
            firstCommit = "${uuid4()}",
            authors = listOf("${uuid4()}"),
            dateTime = Clock.System.now(),
            ease = 7,
            storyId = "${uuid4()}",
            semver = "${uuid4()}",
            label = "${uuid4()}",
            firstCommitDateTime = Clock.System.now() - 20.minutes,
        )
        val partyId = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also { it.givenAny(SaveContributionCommandWrapper::class, VoidResult.Accepted) }
        val command = SaveContribution(exerciseScope, cannon, Clock.System)
            .context { obj = ContributionContext(partyId, "local") }
        val labelOverride = uuidString()
        val linkOverride = uuidString()
        val expectedCycleTime = 127.minutes
    }) exercise {
        command.test("--label $labelOverride --link $linkOverride --cycle-time \"$expectedCycleTime\" --input-json \'${sourceContribution.toJsonString()}\'")
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.stderr)
        receivedActions.firstOrNull()
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
                    firstCommitDateTime = sourceContribution.firstCommitDateTime,
                    cycleTime = expectedCycleTime,
                ),
            )
    }

    @Test
    fun canUseCommitTimeRangeAsCycleTime() = asyncSetup(object : ScopeMint() {
        val now = Clock.System.now()
        val expectedCycleTime = 20.minutes
        val dateTime = now.minus(5.minutes)
        val firstCommitDateTime = now - expectedCycleTime
        val sourceContribution = Contribution(
            lastCommit = "${uuid4()}",
            firstCommit = "${uuid4()}",
            authors = listOf("${uuid4()}"),
            dateTime = dateTime,
            ease = 7,
            storyId = "${uuid4()}",
            semver = "${uuid4()}",
            label = "${uuid4()}",
            firstCommitDateTime = firstCommitDateTime,
        )
        val partyId = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also { it.givenAny(SaveContributionCommandWrapper::class, VoidResult.Accepted) }
        val command = SaveContribution(
            exerciseScope,
            cannon,
            object : Clock {
                override fun now(): Instant = now
            },
        )
            .context { obj = ContributionContext(partyId, "local") }
    }) exercise {
        command.test("--cycle-time-from-first-commit --input-json \'${sourceContribution.toJsonString()}\'")
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.stderr)
        receivedActions.firstOrNull()
            ?.let { it as? SaveContributionCommand }
            ?.cycleTime
            .assertIsEqualTo(expectedCycleTime)
    }

    @Test
    fun usingCommitTimeRangeAsCycleTimeWillWarnWhenOneIsMissing() = asyncSetup(object : ScopeMint() {
        val sourceContribution = Contribution(
            lastCommit = "${uuid4()}",
            firstCommit = "${uuid4()}",
            authors = listOf("${uuid4()}"),
            dateTime = Clock.System.now(),
            ease = 7,
            storyId = "${uuid4()}",
            semver = "${uuid4()}",
            label = "${uuid4()}",
            firstCommitDateTime = null,
        )
        val partyId = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also { it.givenAny(SaveContributionCommandWrapper::class, VoidResult.Accepted) }
        val command = SaveContribution(exerciseScope, cannon, Clock.System)
            .context { obj = ContributionContext(partyId, "local") }
    }) exercise {
        command.test("--cycle-time-from-first-commit --input-json \'${sourceContribution.toJsonString()}\'")
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.stderr)
        result.stdout.assertIsEqualTo("Warning: could not calculate cycle time from missing firstCommitDateTime\n")
        receivedActions.firstOrNull()
            ?.let { it as? SaveContributionCommand }
            ?.cycleTime
            .assertIsEqualTo(null)
    }
}
