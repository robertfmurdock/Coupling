package com.zegreatrob.coupling.cli.party

import com.zegreatrob.coupling.model.ContributionId
import com.zegreatrob.coupling.model.ContributionInput
import com.zegreatrob.tools.digger.model.Contribution
import kotlinx.datetime.toStdlibInstant
import kotools.types.text.toNotBlankString
import kotlin.time.Duration

fun Contribution.contributionInput(
    cycleTime: Duration?,
    link: String?,
    label: String?,
) = ContributionInput(
    contributionId = ContributionId(firstCommit.toNotBlankString().getOrThrow()),
    participantEmails = authors.toSet(),
    hash = lastCommit,
    dateTime = dateTime?.toStdlibInstant(),
    ease = ease,
    story = storyId?.ifBlank { null },
    link = link,
    semver = semver,
    label = label ?: this@contributionInput.label,
    firstCommit = firstCommit,
    firstCommitDateTime = firstCommitDateTime?.toStdlibInstant(),
    cycleTime = cycleTime,
    commitCount = commitCount,
    name = tagName,
    integrationDateTime = tagDateTime?.toStdlibInstant(),
)
