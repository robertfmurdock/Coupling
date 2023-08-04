@file:UseSerializers(DateTimeSerializer::class, PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserDetails
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class JsonUserDetails(
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
    val timestamp: Instant,
)

fun User.toSerializable() = JsonUser(
    id = id,
    details = details?.toSerializable(),
    boost = boost?.toSerializable(),
)

fun JsonUser.toModel() = User(
    id = id,
    details = details?.toModel(),
    boost = boost?.toModelRecord(),
)

fun UserDetails.toSerializable() = JsonUserDetails(
    id = id,
    email = email,
    authorizedPartyIds = authorizedPartyIds,
)

fun Record<UserDetails>.toSerializable() = JsonUserRecord(
    id = data.id,
    email = data.email,
    authorizedPartyIds = data.authorizedPartyIds,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun JsonUserDetails.toModel() = UserDetails(
    id = id,
    email = email,
    authorizedPartyIds = authorizedPartyIds,
    stripeCustomerId = null,
)

fun JsonUserRecord.toModel() = Record(
    data = UserDetails(
        id = id,
        email = email,
        authorizedPartyIds = authorizedPartyIds,
        stripeCustomerId = null,
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
