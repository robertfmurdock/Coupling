@file:UseSerializers(PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import kotlinx.datetime.Instant
import kotlinx.serialization.UseSerializers

interface JsonPartyRecordInfo {
    val partyId: PartyId?
    val modifyingUserEmail: String?
    val isDeleted: Boolean?
    val timestamp: Instant?
}

fun Pin.toSerializable() = GqlPin(
    id = id,
    name = name,
    icon = icon,
)

fun Pin.toSerializableInput() = GqlPinInput(
    id = id,
    name = name,
    icon = icon,
)

fun Record<PartyElement<Pin>>.toSerializable() = GqlPinDetails(
    id = data.element.id,
    name = data.element.name,
    icon = data.element.icon,
    partyId = data.partyId.value,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun GqlPin.toModel(): Pin = Pin(
    id = id,
    name = name ?: "",
    icon = icon ?: "",
)

fun GqlPinInput.toModel(): Pin = Pin(
    id = id,
    name = name ?: "",
    icon = icon ?: "",
)

fun GqlPinDetails.toModel(): Record<PartyElement<Pin>> = Record(
    data = PartyId(partyId).with(Pin(id = id ?: "", name = name, icon = icon)),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
