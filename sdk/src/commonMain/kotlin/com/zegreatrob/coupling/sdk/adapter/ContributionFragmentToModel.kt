package com.zegreatrob.coupling.sdk.adapter

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.sdk.schema.fragment.ContributionFragment

fun ContributionFragment.toModel() = Contribution(
    commitCount = commitCount,
    createdAt = createdAt,
    cycleTime = cycleTime,
    dateTime = dateTime,
    ease = ease,
    firstCommit = firstCommit,
    firstCommitDateTime = firstCommitDateTime,
    hash = hash,
    id = id,
    integrationDateTime = integrationDateTime,
    label = label,
    link = link,
    name = name,
    participantEmails = participantEmails.toSet(),
    semver = semver,
    story = story,
)
