package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.gql.fragment.ContributionFragment
import com.zegreatrob.coupling.model.Contribution

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
