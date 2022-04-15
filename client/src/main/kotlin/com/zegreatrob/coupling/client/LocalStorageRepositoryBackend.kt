package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.user.User

class LocalStorageRepositoryBackend {
    val party by localBackend(List<Record<Party>>::toSerializableString, String::toPartyRecords)
    val player by localBackend(List<PartyRecord<Player>>::toSerializableString, String::toPlayerRecords)
    val pairAssignments by localBackend(
        List<PartyRecord<PairAssignmentDocument>>::toSerializableString,
        String::toPairAssignmentRecords
    )
    val pin by localBackend(List<PartyRecord<Pin>>::toSerializableString, String::toPinRecords)
    val user by localBackend(List<Record<User>>::toSerializableString, String::toUserRecords)
}

fun List<Record<Party>>.toSerializableString() = map { it.toSerializable() }.toJsonString()
fun String.toPartyRecords(): List<Record<Party>> = fromJsonString<List<JsonPartyRecord>>().map { it.toModelRecord() }

fun List<PartyRecord<Player>>.toSerializableString() = map { it.toSerializable() }.toJsonString()
fun String.toPlayerRecords(): List<PartyRecord<Player>> = fromJsonString<List<JsonPlayerRecord>>().map { it.toModel() }

fun List<PartyRecord<PairAssignmentDocument>>.toSerializableString() = map { it.toSerializable() }.toJsonString()
fun String.toPairAssignmentRecords(): List<PartyRecord<PairAssignmentDocument>> =
    fromJsonString<List<JsonPairAssignmentDocumentRecord>>().map { it.toModel() }

fun List<PartyRecord<Pin>>.toSerializableString() = map { it.toSerializable() }.toJsonString()
fun String.toPinRecords(): List<PartyRecord<Pin>> = fromJsonString<List<JsonPinRecord>>().map { it.toModel() }

fun List<Record<User>>.toSerializableString() = map { it.toSerializable() }.toJsonString()
fun String.toUserRecords(): List<Record<User>> = fromJsonString<List<JsonUserRecord>>().map { it.toModel() }
