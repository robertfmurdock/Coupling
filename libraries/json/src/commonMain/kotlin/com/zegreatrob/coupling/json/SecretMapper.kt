package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.with

fun GqlPartySecret.toModel(): PartyRecord<Secret> = PartyRecord(
    PartyId(partyId).with(
        Secret(
            id = id,
            createdTimestamp = createdTimestamp,
            description = description,
            lastUsedTimestamp = lastUsedTimestamp,
        ),
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun PartyRecord<Secret>.toSerializable() = GqlPartySecret(
    id = data.element.id,
    createdTimestamp = data.element.createdTimestamp,
    lastUsedTimestamp = data.element.lastUsedTimestamp,
    description = data.element.description,
    partyId = data.partyId.value,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
