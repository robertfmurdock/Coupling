package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyIntegration

fun Record<PartyIntegration>.toSerializable() = GqlPartyIntegration(
    slackTeam = data.slackTeam,
    slackChannel = data.slackChannel,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun GqlPartyIntegration.toModelRecord(): Record<PartyIntegration> = Record(
    data = PartyIntegration(
        slackTeam = slackTeam,
        slackChannel = slackChannel,
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
