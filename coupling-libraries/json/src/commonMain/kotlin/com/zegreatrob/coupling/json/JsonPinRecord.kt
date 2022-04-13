@file:UseSerializers(DateTimeSerializer::class, TribeIdSerializer::class)
package com.zegreatrob.coupling.json

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class JsonPinRecord(
    val id: String? = null,
    val name: String = "",
    val icon: String = "",
    override val tribeId: PartyId,
    override val modifyingUserEmail: String,
    override val isDeleted: Boolean,
    override val timestamp: DateTime,
) : JsonTribeRecordInfo

interface JsonTribeRecordInfo {
    val tribeId: PartyId?
    val modifyingUserEmail: String?
    val isDeleted: Boolean?
    val timestamp: DateTime?
}

interface JsonPin {
    val id: String?
    val name: String
    val icon: String
}

@Serializable
data class JsonPinData(override val id: String?, override val name: String, override val icon: String) : JsonPin

@Serializable
data class SavePinInput(
    override val tribeId: PartyId,
    val pinId: String?,
    val name: String,
    val icon: String,
): TribeInput

fun Pin.toSerializable() = JsonPinData(
    id = id,
    name = name,
    icon = icon,
)

fun Record<PartyElement<Pin>>.toSerializable() = JsonPinRecord(
    id = data.element.id,
    name = data.element.name,
    icon = data.element.icon,
    tribeId = data.id,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun JsonPinData.toModel(): Pin = Pin(
    id = id,
    name = name,
    icon = icon,
)

fun JsonPinRecord.toModel(): Record<PartyElement<Pin>> = Record(
    data = tribeId.with(Pin(id = id, name = name, icon = icon)),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp
)
