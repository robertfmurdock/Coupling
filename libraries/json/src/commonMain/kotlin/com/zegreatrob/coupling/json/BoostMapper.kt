package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record

fun GqlBoostDetails.toModelRecord(): Record<Boost> = Record(
    data = Boost(
        userId = userId,
        partyIds = partyIds.toSet(),
        expirationDate = expirationDate,
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun Record<Boost>.toSerializable() = GqlBoostDetails(
    userId = data.userId,
    partyIds = data.partyIds.toList(),
    expirationDate = data.expirationDate,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
