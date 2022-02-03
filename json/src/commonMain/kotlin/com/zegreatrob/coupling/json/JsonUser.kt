package com.zegreatrob.coupling.json

import com.soywiz.klock.ISO8601
import com.soywiz.klock.parse
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import kotlinx.serialization.Serializable

@Serializable
data class JsonUser(
    val id: String,
    val email: String,
    val authorizedTribeIds: Set<String>
)

@Serializable
data class JsonUserRecord(
    val id: String,
    val email: String,
    val authorizedTribeIds: Set<String>,
    val modifyingUserEmail: String,
    val isDeleted: Boolean,
    val timestamp: String,
)

fun User.toSerializable() = JsonUser(
    id = id,
    email = email,
    authorizedTribeIds = authorizedTribeIds.map { it.value }.toSet()
)

fun Record<User>.toSerializable() = JsonUserRecord(
    id = data.id,
    email = data.email,
    authorizedTribeIds = data.authorizedTribeIds.map { it.value }.toSet(),
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp.format(ISO8601.DATETIME_UTC_COMPLETE_FRACTION),
)

fun JsonUser.toModel() = User(
    id = id,
    email = email,
    authorizedTribeIds = authorizedTribeIds.map(::TribeId).toSet(),
)

fun JsonUserRecord.toModel() = Record(
    data = User(
        id = id,
        email = email,
        authorizedTribeIds = authorizedTribeIds.map(::TribeId).toSet(),
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = ISO8601.DATETIME_UTC_COMPLETE_FRACTION.parse(timestamp).local,
)