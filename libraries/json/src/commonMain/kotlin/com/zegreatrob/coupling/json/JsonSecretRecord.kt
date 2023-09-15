@file:UseSerializers(DateTimeSerializer::class, PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.with
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class JsonSecretRecord(
    val id: String,
    val description: String,
    val createdTimestamp: Instant,
    override val partyId: PartyId,
    override val modifyingUserEmail: String,
    override val isDeleted: Boolean,
    override val timestamp: Instant,
) : JsonPartyRecordInfo

fun JsonSecretRecord.toModel(): PartyRecord<Secret> = PartyRecord(
    partyId.with(Secret(id = id, createdTimestamp = createdTimestamp, description = description)),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun PartyRecord<Secret>.toSerializable() = JsonSecretRecord(
    id = data.element.id,
    createdTimestamp = data.element.createdTimestamp,
    description = data.element.description,
    partyId = data.partyId,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
