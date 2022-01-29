package com.zegreatrob.coupling.json

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.serialization.Serializable

@Serializable
data class JsonBoostRecord(
    val id: String,
    val userId: String,
    val tribeIds: Set<String>,
    val modifyingUserEmail: String,
    val isDeleted: Boolean,
    val timestamp: String,
)

fun JsonBoostRecord.toModelRecord(): Record<Boost> = Record(
    data = Boost(id, userId, tribeIds.map { TribeId(it) }.toSet()),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = DateTime.fromString(timestamp).local
)