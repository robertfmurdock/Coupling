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
            val pairs = pairsContributions.toMap().keys.filter { it.count() == 2 }

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
            }
                .groupBy { it.first }
                .mapValues { group -> group.value.flatMap { it.second } }
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
