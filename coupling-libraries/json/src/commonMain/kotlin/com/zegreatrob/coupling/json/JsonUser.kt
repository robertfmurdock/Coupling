@file:UseSerializers(DateTimeSerializer::class, PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.User
import korlibs.time.DateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class JsonUser(
    val id: String,
    val email: String,
    val authorizedPartyIds: Set<PartyId>,
)

@Serializable
data class JsonUserRecord(
    val id: String,
    val email: String,
    val authorizedPartyIds: Set<PartyId>,
    val modifyingUserEmail: String,
    val isDeleted: Boolean,
    val timestamp: DateTime,
)

fun User.toSerializable() = JsonUser(
    id = id,
    email = email,
    authorizedPartyIds = authorizedPartyIds,
)

fun Record<User>.toSerializable() = JsonUserRecord(
    id = data.id,
    email = data.email,
    authorizedPartyIds = data.authorizedPartyIds,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun JsonUser.toModel() = User(
    id = id,
    email = email,
    authorizedPartyIds = authorizedPartyIds,
)

fun JsonUserRecord.toModel() = Record(
    data = User(
        id = id,
        email = email,
        authorizedPartyIds = authorizedPartyIds,
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
