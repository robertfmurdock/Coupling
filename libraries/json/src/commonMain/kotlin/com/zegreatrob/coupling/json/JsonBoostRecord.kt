@file:UseSerializers(PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.serialization.UseSerializers

fun GqlBoostDetails.toModelRecord(): Record<Boost> = Record(
    data = Boost(
        userId = userId,
        partyIds = partyIds.map(::PartyId).toSet(),
        expirationDate = expirationDate,
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun Record<Boost>.toSerializable() = GqlBoostDetails(
    userId = data.userId,
    partyIds = data.partyIds.map(PartyId::value),
    expirationDate = data.expirationDate,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
