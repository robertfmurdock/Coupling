package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserDetails
import kotlinx.serialization.Serializable
import kotools.types.text.NotBlankString
import kotools.types.text.toNotBlankString
import kotlin.time.Instant

@Serializable
data class JsonUserRecord(
    val id: UserIdString,
    val email: String,
    val authorizedPartyIds: Set<String>,
    val modifyingUserEmail: String,
    val isDeleted: Boolean,
    val timestamp: Instant,
)

fun Record<UserDetails>.toSerializable() = JsonUserRecord(
    id = data.id,
    email = data.email.toString(),
    authorizedPartyIds = data.authorizedPartyIds.map(PartyId::value).map(NotBlankString::toString).toSet(),
    modifyingUserEmail = modifyingUserId?.toString() ?: "",
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun JsonUserRecord.toModel() = Record(
    data = UserDetails(
        id = id,
        email = email.toNotBlankString().getOrThrow(),
        authorizedPartyIds = authorizedPartyIds.map(::PartyId).toSet(),
        stripeCustomerId = null,
        connectSecretId = null,
    ),
    modifyingUserId = modifyingUserEmail.toNotBlankString().getOrNull(),
    isDeleted = isDeleted,
    timestamp = timestamp,
)
