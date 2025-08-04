package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.obj
import com.github.ajalt.clikt.testing.test
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.SaveContributionCommandWrapper
import com.zegreatrob.coupling.cli.party.BatchContribution
import com.zegreatrob.coupling.cli.party.ContributionContext
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.testaction.StubCannon
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.tools.digger.json.toJsonString
import com.zegreatrob.tools.digger.model.Contribution
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

class BatchContributionCLITest {

    @Test
    fun whenNoTagDateTimeCycleTimeWillBeNull() = asyncSetup(object : ScopeMint() {
        val now = Clock.System.now()
        val expectedCycleTime = 20.minutes
        val dateTime = now.minus(5.minutes)
        val firstCommitDateTime = now - expectedCycleTime
        val sourceContribution = Contribution(
            lastCommit = "${Uuid.random()}",
            firstCommit = "${Uuid.random()}",
            authors = listOf("${Uuid.random()}"),
            dateTime = dateTime,
            ease = 7,
            storyId = "${Uuid.random()}",
            semver = "${Uuid.random()}",
            label = "${Uuid.random()}",
            firstCommitDateTime = firstCommitDateTime,
            tagName = "${Uuid.random()}",
            tagDateTime = null,
            commitCount = 3214,
        )
        val partyId = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also { it.givenAny(SaveContributionCommandWrapper::class, VoidResult.Accepted) }
        val command = BatchContribution(
            exerciseScope,
            cannon,
            object : Clock {
                override fun now(): Instant = now
            },
        )
            .context { obj = ContributionContext(partyId, "local") }
    }) exercise {
        command.test("--cycle-time-from-first-commit --input-json \'${listOf(sourceContribution).toJsonString()}\'")
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.stderr)
        receivedActions.firstOrNull()
            ?.let { it as? SaveContributionCommand }
            ?.contributionList
            ?.firstOrNull()
            ?.cycleTime
            .assertIsEqualTo(null)
    }

    @Test
    fun whenTagDateTimeCanUseCommitTimeRangeAsCycleTimeUntilTagDateTime() = asyncSetup(object : ScopeMint() {
        val expectedCycleTime = 20.minutes
        val dateTime = Clock.System.now().minus(5.minutes)
        val tagDateTime = dateTime.minus(2.minutes)
        val firstCommitDateTime = tagDateTime - expectedCycleTime
        val sourceContribution = Contribution(
            lastCommit = "${Uuid.random()}",
            firstCommit = "${Uuid.random()}",
            authors = listOf("${Uuid.random()}"),
            dateTime = dateTime,
            ease = 7,
            storyId = "${Uuid.random()}",
            semver = "${Uuid.random()}",
            label = "${Uuid.random()}",
            firstCommitDateTime = firstCommitDateTime,
            tagName = "${Uuid.random()}",
            tagDateTime = tagDateTime,
            commitCount = 3214,
        )
        val partyId = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also { it.givenAny(SaveContributionCommandWrapper::class, VoidResult.Accepted) }
        val command = BatchContribution(exerciseScope, cannon, Clock.System)
            .context { obj = ContributionContext(partyId, "local") }
    }) exercise {
        command.test("--cycle-time-from-first-commit --input-json \'${listOf(sourceContribution).toJsonString()}\'")
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.stderr)
        receivedActions.firstOrNull()
            ?.let { it as? SaveContributionCommand }
            ?.contributionList
            ?.firstOrNull()
            ?.cycleTime
            .assertIsEqualTo(expectedCycleTime)
    }

    @Test
    fun usingCommitTimeRangeAsCycleTimeWillWarnWhenOneIsMissing() = asyncSetup(object : ScopeMint() {
        val sourceContribution = Contribution(
            lastCommit = "${Uuid.random()}",
            firstCommit = "${Uuid.random()}",
            authors = listOf("${Uuid.random()}"),
            dateTime = Clock.System.now(),
            ease = 7,
            storyId = "${Uuid.random()}",
            semver = "${Uuid.random()}",
            label = "${Uuid.random()}",
            firstCommitDateTime = null,
            tagDateTime = Clock.System.now().plus(43.minutes),
            tagName = "Bill",
            commitCount = 654,
        )
        val partyId = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also { it.givenAny(SaveContributionCommandWrapper::class, VoidResult.Accepted) }
        val command = BatchContribution(exerciseScope, cannon, Clock.System)
            .context { obj = ContributionContext(partyId, "local") }
    }) exercise {
        command.test("--cycle-time-from-first-commit --input-json \'${listOf(sourceContribution.toJsonString())}\'")
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.stderr)
        result.stdout.assertIsEqualTo("Warning: could not calculate cycle time from missing firstCommitDateTime\n")
        receivedActions.firstOrNull()
            ?.let { it as? SaveContributionCommand }
            ?.contributionList
            ?.firstOrNull()
            ?.cycleTime
            .assertIsEqualTo(null)
    }
}
