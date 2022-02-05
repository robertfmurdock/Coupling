@file:UseSerializers(DateTimeSerializer::class)
package com.zegreatrob.coupling.json

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class JsonBoostRecord(
    val userId: String,
    val tribeIds: Set<String>,
    val modifyingUserEmail: String,
    val isDeleted: Boolean,
    val timestamp: DateTime,
)

fun JsonBoostRecord.toModelRecord(): Record<Boost> = Record(
    data = Boost(userId, tribeIds.map { TribeId(it) }.toSet()),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp
)

fun Record<Boost>.toSerializable() = JsonBoostRecord(
    userId = data.userId,
    tribeIds = data.tribeIds.map(TribeId::value).toSet(),
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp
)
