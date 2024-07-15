package com.zegreatrob.coupling.client.components.stats

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.json.JsonContributionWindow
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

private val random = Random(10)

fun generateFakeContributions(
    pairsContributions: List<Pair<CouplingPair, List<Contribution>>>,
    selectedWindow: JsonContributionWindow,
): List<Pair<CouplingPair, List<Contribution>>> =
    contributionStartDateTime(selectedWindow, pairsContributions)
        .let { startDateTime ->
            val datesUntilNow = (1..(startDateTime.daysUntil(Clock.System.now(), TimeZone.currentSystemDefault())))
                .map { dayCount -> (startDateTime + dayCount.days).toLocalDateTime(TimeZone.currentSystemDefault()) }
            val pairs = pairsContributions.toMap().keys

            datesUntilNow.flatMap { date ->
                if (setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(date.dayOfWeek)) {
                    return@flatMap emptyList()
                }

                val pairingSet = pairs.shuffled().fold(emptyList<CouplingPair>()) { pairingSet, couplingPair ->
                    if (couplingPair.any { pairingSet.flatten().contains(it) }) {
                        pairingSet
                    } else {
                        pairingSet + listOf(couplingPair)
                    }
                }
                pairingSet.map { pair ->
                    pair to generateSequence { date.toFakeContribution() }.take(random.nextInt(0, 5)).toList()
                }
            }.toMap()
        }.let { updated ->
            pairsContributions.map { (pair) ->
                pair to (updated[pair] ?: emptyList())
            }
        }

private fun contributionStartDateTime(
    selectedWindow: JsonContributionWindow,
    pairsContributions: List<Pair<CouplingPair, List<Contribution>>>,
) = beginningOfWindow(selectedWindow) ?: pairsContributions.toMap().values.flatten().firstContributionInstant()

private fun beginningOfWindow(selectedWindow: JsonContributionWindow) = selectedWindow.toModel()?.let {
    Clock.System.now() - it
}

// pairsContributions.map { it.first to generateFakeContributions(selectedWindow) }

private fun generateFakeContributions(selectedWindow: JsonContributionWindow) =
    generateCommitTimes()
        .map(LocalDateTime::toFakeContribution)

private fun LocalDateTime.toFakeContribution() = Contribution(
    id = "${uuid4()}",
    createdAt = Clock.System.now(),
    dateTime = toInstant(TimeZone.currentSystemDefault()),
    hash = null,
    firstCommit = null,
    ease = null,
    story = null,
    link = null,
    participantEmails = emptySet(),
    label = if (Random.nextBoolean()) "fake" else "alternate",
    semver = null,
)

private fun generateCommitTimes(): List<LocalDateTime> {
    val today = Clock.System.now()
    val numberOfDays = 30
    val monthAgo = today.minus(duration = numberOfDays.days)

    val commitTimes = (1..numberOfDays).flatMap { day ->
        if (day.isWeekday()) {
            val dayDate = monthAgo.plus(day.days)
            val numberOfCommits = random.nextInt(0, 6)
            (0..numberOfCommits).map {
                val timeOfDay = random.nextInt(9, 17)
                dayDate.toLocalDateTime(TimeZone.currentSystemDefault()).date.atTime(timeOfDay, 0, 0)
            }
        } else {
            emptyList()
        }
    }
    return commitTimes
}

private fun Int.isWeekday() = (1..5).contains(dayOfWeek())
private fun Int.dayOfWeek() = this % 7
