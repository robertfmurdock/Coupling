@file:UseSerializers(DateTimeSerializer::class)
package com.zegreatrob.coupling.json

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

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
    val timestamp: DateTime,
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
    timestamp = timestamp,
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
    timestamp = timestamp,
)