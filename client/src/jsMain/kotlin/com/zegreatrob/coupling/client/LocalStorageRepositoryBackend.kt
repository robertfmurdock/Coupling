package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.json.JsonPairAssignmentDocumentRecord
import com.zegreatrob.coupling.json.JsonPartyDetailsRecord
import com.zegreatrob.coupling.json.JsonPinRecord
import com.zegreatrob.coupling.json.JsonPlayerRecord
import com.zegreatrob.coupling.json.fromJsonString
import com.zegreatrob.coupling.json.toJsonString
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toModelRecord
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player

class LocalStorageRepositoryBackend {
    val party by localBackend(List<Record<PartyDetails>>::toSerializableString, String::toPartyRecords)
    val player by localBackend(List<PartyRecord<Player>>::toSerializableString, String::toPlayerRecords)
    val pairAssignments by localBackend(
        List<PartyRecord<PairAssignmentDocument>>::toSerializableString,
        String::toPairAssignmentRecords,
    )
    val pin by localBackend(List<PartyRecord<Pin>>::toSerializableString, String::toPinRecords)
}

fun List<Record<PartyDetails>>.toSerializableString() = map { it.toSerializable() }.toJsonString()
fun String.toPartyRecords(): List<Record<PartyDetails>> =
    fromJsonString<List<JsonPartyDetailsRecord>>().mapNotNull { it.toModelRecord() }

fun List<PartyRecord<Player>>.toSerializableString() = map { it.toSerializable() }.toJsonString()
fun String.toPlayerRecords(): List<PartyRecord<Player>> = fromJsonString<List<JsonPlayerRecord>>().map { it.toModel() }

fun List<PartyRecord<PairAssignmentDocument>>.toSerializableString() = map { it.toSerializable() }.toJsonString()
fun String.toPairAssignmentRecords(): List<PartyRecord<PairAssignmentDocument>> =
    fromJsonString<List<JsonPairAssignmentDocumentRecord>>().map { it.toModel() }

fun List<PartyRecord<Pin>>.toSerializableString() = map { it.toSerializable() }.toJsonString()
fun String.toPinRecords(): List<PartyRecord<Pin>> = fromJsonString<List<JsonPinRecord>>().map { it.toModel() }
