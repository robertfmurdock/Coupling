package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with

fun PartyRecord<Contribution>.toJson() = GqlContribution(
    id = data.element.id,
    createdAt = data.element.createdAt,
    dateTime = data.element.dateTime,
    hash = data.element.hash,
    ease = data.element.ease,
    story = data.element.story,
    link = data.element.link,
    semver = data.element.semver,
    label = data.element.label,
    firstCommit = data.element.firstCommit,
    firstCommitDateTime = data.element.firstCommitDateTime,
    participantEmails = data.element.participantEmails.toList(),
    integrationDateTime = data.element.integrationDateTime,
    cycleTime = data.element.cycleTime,
    partyId = data.partyId.value,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun GqlContribution.toModel() = PartyRecord(
    data = PartyId(id).with(
        Contribution(
            id = id,
            createdAt = createdAt,
            dateTime = dateTime,
            hash = hash,
            ease = ease,
            story = story,
            link = link,
            participantEmails = participantEmails.toSet(),
            label = label,
            semver = semver,
            firstCommit = firstCommit,
            firstCommitDateTime = firstCommitDateTime,
            integrationDateTime = integrationDateTime,
            cycleTime = cycleTime,
        ),
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
