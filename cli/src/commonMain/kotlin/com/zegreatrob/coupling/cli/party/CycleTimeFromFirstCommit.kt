package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.core.CliktCommand
import com.zegreatrob.tools.digger.model.Contribution
import kotlinx.datetime.Instant
import kotlin.time.Duration

fun CliktCommand.cycleTimeFromFirstCommit(contribution: Contribution, now: Instant?): Duration? {
    val firstCommitDateTime = contribution.firstCommitDateTime
    return if (firstCommitDateTime == null) {
        echo("Warning: could not calculate cycle time from missing firstCommitDateTime")
        null
    } else {
        (contribution.tagDateTime ?: now)?.let { it - firstCommitDateTime }
    }
}
