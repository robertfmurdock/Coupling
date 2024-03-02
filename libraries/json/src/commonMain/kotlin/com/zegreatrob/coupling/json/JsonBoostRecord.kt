@file:UseSerializers(PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class JsonBoostRecord(
    val userId: String,
    val partyIds: Set<PartyId>,
    val expirationDate: Instant,
    val modifyingUserEmail: String,
    val isDeleted: Boolean,
    val timestamp: Instant,
)

fun JsonBoostRecord.toModelRecord(): Record<Boost> = Record(
    data = Boost(
        userId = userId,
        partyIds = partyIds,
        expirationDate = expirationDate,
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun Record<Boost>.toSerializable() = JsonBoostRecord(
    userId = data.userId,
    partyIds = data.partyIds,
    expirationDate = data.expirationDate,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
