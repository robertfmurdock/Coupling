package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.action.pairassignmentdocument.CreatePairCandidateReportAction
import com.zegreatrob.coupling.action.pairassignmentdocument.CreatePairCandidateReportListAction
import com.zegreatrob.coupling.action.pairassignmentdocument.FindNewPairsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.Game
import com.zegreatrob.coupling.action.pairassignmentdocument.NextPlayerAction
import com.zegreatrob.coupling.action.pairassignmentdocument.Wheel
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionId
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.partyRecord
import com.zegreatrob.testmints.action.DispatcherPipeCannon
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotools.types.collection.toNotEmptyList
import kotools.types.text.toNotBlankString
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

private val random = Random(10)

suspend fun generateFakeContributions(
    pairsContributions: List<Pair<CouplingPair, ContributionReport>>,
    selectedWindow: GqlContributionWindow,
    fakeStyle: FakeDataStyle,
): List<Pair<CouplingPair, ContributionReport>> = contributionStartDateTime(selectedWindow, pairsContributions)
    .let { startDateTime ->
        val datesUntilNow = (1..(startDateTime.daysUntil(Clock.System.now(), TimeZone.currentSystemDefault())))
            .map { dayCount -> (startDateTime + dayCount.days).toLocalDateTime(TimeZone.currentSystemDefault()) }

        when (fakeStyle) {
            FakeDataStyle.RandomPairs -> generatePairsRandomlyNoSolos(pairsContributions, datesUntilNow, true)
            FakeDataStyle.RandomPairsWithRandomSolos -> generatePairsRandomlyNoSolos(
                pairsContributions,
                datesUntilNow,
                false,
            )

            FakeDataStyle.StrongPairingTeam -> generateStrongPairingTeam(pairsContributions, datesUntilNow)
        }
    }.let { updated ->
        pairsContributions.map { (pair) ->
            val contributions = updated[pair] ?: emptyList()
            pair to ContributionReport(
                contributions = contributions.map {
                    partyRecord(
                        partyId = PartyId("-"),
                        data = it,
                        modifyingUserEmail = "-".toNotBlankString().getOrThrow(),
                    )
                },
                medianCycleTime = contributions.getOrNull(contributions.size / 2)?.cycleTime,
                withCycleTimeCount = contributions.mapNotNull { it.cycleTime }.count(),
            )
        }
    }

private fun generatePairsRandomlyNoSolos(
    pairsContributions: List<Pair<CouplingPair, ContributionReport>>,
    datesUntilNow: List<LocalDateTime>,
    noSolos: Boolean,
): Map<CouplingPair, List<Contribution>> {
    val pairs = pairsContributions.toMap().keys.filter(
        if (noSolos) (onlyPairs) else ({ true }),
    )

    return datesUntilNow.flatMap { date ->
        if (setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(date.dayOfWeek)) {
            return@flatMap emptyList()
        }

        val pairingSet = pairs.shuffled().generatePairingSet()
        pairingSet.map { pair ->
            pair to generateSequence { date.toFakeContribution() }.take(random.nextInt(0, 5)).toList()
        }
    }
        .groupBy { it.first }
        .mapValues { group -> group.value.flatMap { it.second } }
}

private val onlyPairs: (CouplingPair) -> Boolean = { it.count() == 2 }

private suspend fun generateStrongPairingTeam(
    pairsContributions: List<Pair<CouplingPair, ContributionReport>>,
    datesUntilNow: List<LocalDateTime>,
): Map<CouplingPair, List<Contribution>> {
    val pairs = pairsContributions.toMap().keys
    val players = pairs.flatten().toNotEmptyList().getOrNull() ?: return emptyMap()

    val dispatcher = FakeContributionDispatcher()

    val populatedHistory = datesUntilNow.fold(emptyList<PairAssignmentDocument>()) { history, date ->
        if (setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(date.dayOfWeek)) {
            return@fold history
        }
        val pairAssignmentsNewestFirst = PairAssignmentDocument(
            id = PairAssignmentDocumentId.new(),
            date = date.toInstant(TimeZone.currentSystemDefault()),
            pairs = dispatcher.perform(FindNewPairsAction(Game(players, history, PairingRule.LongestTime))).withPins(),
        )
        listOf(pairAssignmentsNewestFirst) + history
    }
    return populatedHistory
        .flatMap { pairAssignments ->
            pairAssignments.pairs.map<PinnedCouplingPair, CouplingPair>(PinnedCouplingPair::toPair)
                .map<CouplingPair, Pair<CouplingPair, List<Contribution>>> { pair ->
                    pair to generateSequence {
                        pairAssignments.date.toLocalDateTime(TimeZone.currentSystemDefault()).toFakeContribution()
                    }.take(random.nextInt(1, 8)).toList()
                }.toList()
        }
        .groupBy { it.first }
        .mapValues { group -> group.value.flatMap { it.second } }
}

class FakeContributionDispatcher :
    FindNewPairsAction.Dispatcher<FakeContributionDispatcher>,
    CreatePairCandidateReportAction.Dispatcher,
    CreatePairCandidateReportListAction.Dispatcher<FakeContributionDispatcher>,
    NextPlayerAction.Dispatcher<FakeContributionDispatcher>,
    Wheel {
    override val wheel = this
    override val cannon = DispatcherPipeCannon(this)
}

private fun List<CouplingPair>.generatePairingSet(): List<CouplingPair> = fold(emptyList()) { pairingSet, couplingPair ->
    if (couplingPair.any { pairingSet.flatten().contains(it) }) {
        pairingSet
    } else {
        pairingSet + listOf(couplingPair)
    }
}

private fun contributionStartDateTime(
    selectedWindow: GqlContributionWindow,
    pairsContributions: List<Pair<CouplingPair, ContributionReport>>,
) = beginningOfWindow(selectedWindow) ?: pairsContributions.toMap()
    .values
    .mapNotNull { it.contributions?.elements }
    .flatten()
    .firstContributionInstant()

private fun beginningOfWindow(selectedWindow: GqlContributionWindow) = selectedWindow.toModel()?.let {
    Clock.System.now() - it
}

private fun LocalDateTime.toFakeContribution() = Contribution(
    id = ContributionId.new(),
    createdAt = Clock.System.now(),
    dateTime = toInstant(TimeZone.currentSystemDefault()),
    hash = null,
    firstCommit = null,
    ease = (1..5).plus(null).random(),
    story = null,
    link = null,
    participantEmails = emptySet(),
    label = if (Random.nextBoolean()) "fake" else "alternate",
    semver = null,
    firstCommitDateTime = this.toInstant(TimeZone.currentSystemDefault()) - 10.minutes,
    integrationDateTime = this.toInstant(TimeZone.currentSystemDefault()) - 20.minutes,
    cycleTime = ((1..12).random() / 4.0).days,
    name = null,
    commitCount = null,
)
