@file:UseSerializers(DateTimeSerializer::class, PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class JsonBoostRecord(
    val userId: String,
    val partyIds: Set<PartyId>,
    val modifyingUserEmail: String,
    val isDeleted: Boolean,
    val timestamp: DateTime,
)

fun JsonBoostRecord.toModelRecord(): Record<Boost> = Record(
    data = Boost(userId, partyIds),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun Record<Boost>.toSerializable() = JsonBoostRecord(
    userId = data.userId,
    partyIds = data.partyIds,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
