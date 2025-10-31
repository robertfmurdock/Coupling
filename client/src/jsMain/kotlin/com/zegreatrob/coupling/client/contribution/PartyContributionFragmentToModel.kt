package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.gql.fragment.PartyContributionFragment
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.partyRecord

fun PartyContributionFragment.toModel() = partyRecord(
    partyId = partyId,
    data = Contribution(
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
    ),
    modifyingUserEmail = modifyingUserEmail!!,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
