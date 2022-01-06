package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.user.User

class LocalStorageRepositoryBackend {
    val tribe by localBackend(List<Record<Tribe>>::toSerializableString, String::toTribeRecords)
    val player by localBackend(List<TribeRecord<Player>>::toSerializableString, String::toPlayerRecords)
    val pairAssignments by localBackend(
        List<TribeRecord<PairAssignmentDocument>>::toSerializableString,
        String::toPairAssignmentRecords
    )
    val pin by localBackend(List<TribeRecord<Pin>>::toSerializableString, String::toPinRecords)
    val user by localBackend(List<Record<User>>::toSerializableString, String::toUserRecords)
}

fun List<Record<Tribe>>.toSerializableString() = map { it.toSerializable() }.toJsonString()
fun String.toTribeRecords(): List<Record<Tribe>> = fromJsonString<List<JsonTribeRecord>>().map { it.toModelRecord() }

fun List<TribeRecord<Player>>.toSerializableString() = map { it.toSerializable() }.toJsonString()
fun String.toPlayerRecords(): List<TribeRecord<Player>> = fromJsonString<List<JsonPlayerRecord>>().map { it.toModel() }

fun List<TribeRecord<PairAssignmentDocument>>.toSerializableString() = map { it.toSerializable() }.toJsonString()
fun String.toPairAssignmentRecords(): List<TribeRecord<PairAssignmentDocument>> =
    fromJsonString<List<JsonPairAssignmentDocumentRecord>>().map { it.toModel() }

fun List<TribeRecord<Pin>>.toSerializableString() = map { it.toSerializable() }.toJsonString()
fun String.toPinRecords(): List<TribeRecord<Pin>> = fromJsonString<List<JsonPinRecord>>().map { it.toModel() }

fun List<Record<User>>.toSerializableString() = map { it.toSerializable() }.toJsonString()
fun String.toUserRecords(): List<Record<User>> = fromJsonString<List<JsonUserRecord>>().map { it.toModel() }
