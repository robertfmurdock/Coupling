package com.zegreatrob.coupling.json

import com.soywiz.klock.DateTime
import com.soywiz.klock.js.toDate
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import kotlinx.serialization.Serializable

@Serializable
data class JsonPinRecord(
    val id: String? = null,
    val name: String = "",
    val icon: String = "",
    override val tribeId: String,
    override val modifyingUserEmail: String,
    override val isDeleted: Boolean,
    override val timestamp: String,
) : JsonTribeRecord

interface JsonTribeRecord {
    val tribeId: String?
    val modifyingUserEmail: String?
    val isDeleted: Boolean?
    val timestamp: String?
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
    override val tribeId: String,
    val pinId: String?,
    val name: String,
    val icon: String,
): TribeInput

@Serializable
data class PinInput(
    override val id: String?,
    override val name: String,
    override val icon: String,
) : JsonPin

fun Pin.toSerializable() = JsonPinData(
    id = id,
    name = name,
    icon = icon,
)

fun Pin.toPinInput() = PinInput(
    id = id,
    name = name,
    icon = icon,
)

fun Record<TribeIdPin>.toSerializable() = JsonPinRecord(
    id = data.element.id,
    name = data.element.name,
    icon = data.element.icon,
    tribeId = data.id.value,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp.toDate().toISOString(),
)

fun JsonPinData.toModel(): Pin = Pin(
    id = id,
    name = name,
    icon = icon,
)

fun JsonPinRecord.toModel(): Record<TribeIdPin> = Record(
    data = TribeId(tribeId).with(Pin(id = id, name = name, icon = icon)),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = DateTime.fromString(timestamp).local
)
