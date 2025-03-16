package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.model.party.with

fun GqlPartySecret.toModel(): PartyRecord<Secret>? = PartyRecord(
    partyId.with(
        Secret(
            id = SecretId(id),
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
    id = data.element.id.value,
    createdTimestamp = data.element.createdTimestamp,
    lastUsedTimestamp = data.element.lastUsedTimestamp,
    description = data.element.description,
    partyId = data.partyId,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
