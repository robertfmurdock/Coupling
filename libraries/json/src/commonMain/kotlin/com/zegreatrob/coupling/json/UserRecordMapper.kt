package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserDetails
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotools.types.text.NotBlankString

@Serializable
data class JsonUserRecord(
    val id: String,
    val email: String,
    val authorizedPartyIds: Set<String>,
    val modifyingUserEmail: String,
    val isDeleted: Boolean,
    val timestamp: Instant,
)

fun Record<UserDetails>.toSerializable() = JsonUserRecord(
    id = data.id,
    email = data.email,
    authorizedPartyIds = data.authorizedPartyIds.map(PartyId::value).map(NotBlankString::toString).toSet(),
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun JsonUserRecord.toModel() = Record(
    data = UserDetails(
        id = id,
        email = email,
        authorizedPartyIds = authorizedPartyIds.map(::PartyId).toSet(),
        stripeCustomerId = null,
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
