@file:UseSerializers(PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class JsonPinRecord(
    val id: String? = null,
    val name: String = "",
    val icon: String = "",
    override val partyId: PartyId,
    override val modifyingUserEmail: String,
    override val isDeleted: Boolean,
    override val timestamp: Instant,
) : JsonPartyRecordInfo

interface JsonPartyRecordInfo {
    val partyId: PartyId?
    val modifyingUserEmail: String?
    val isDeleted: Boolean?
    val timestamp: Instant?
}

@Serializable
data class SavePinInput(
    override val partyId: PartyId,
    val pinId: String?,
    val name: String,
    val icon: String,
) : IPartyInput

fun Pin.toSerializable() = GqlPin(
    id = id,
    name = name,
    icon = icon,
)

fun Record<PartyElement<Pin>>.toSerializable() = JsonPinRecord(
    id = data.element.id,
    name = data.element.name,
    icon = data.element.icon,
    partyId = data.partyId,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun GqlPin.toModel(): Pin = Pin(
    id = id,
    name = name ?: "",
    icon = icon ?: "",
)

fun JsonPinRecord.toModel(): Record<PartyElement<Pin>> = Record(
    data = partyId.with(Pin(id = id ?: "", name = name, icon = icon)),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
