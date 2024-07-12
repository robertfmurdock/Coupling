package com.zegreatrob.coupling.client.stats

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.Contribution
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

fun generateFakeContributions() = generateCommitTimes().map(LocalDateTime::toFakeContribution)

private val random = Random(10)

private fun LocalDateTime.toFakeContribution() = Contribution(
    id = "${uuid4()}",
    createdAt = Clock.System.now(),
    dateTime = toInstant(TimeZone.currentSystemDefault()),
    null,
    null,
    null,
    null,
    null,
    emptySet(),
    if (Random.nextBoolean()) "fake" else "alternate",
    null,
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
