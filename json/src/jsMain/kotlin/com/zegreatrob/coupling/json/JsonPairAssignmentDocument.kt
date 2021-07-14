package com.zegreatrob.coupling.json

import com.soywiz.klock.DateTime
import com.soywiz.klock.js.toDate
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import kotlinx.serialization.Serializable

@Serializable
data class JsonPairAssignmentDocument(
    val id: String,
    val date: String,
    val pairs: List<JsonPinnedCouplingPair>
)

@Serializable
data class JsonPairAssignmentDocumentRecord(
    val id: String,
    val date: String,
    val pairs: List<JsonPinnedCouplingPair>,
    override val tribeId: String?,
    override val modifyingUserEmail: String?,
    override val isDeleted: Boolean?,
    override val timestamp: String?
) : JsonTribeRecord

@Serializable
data class JsonPinnedCouplingPair(val players: List<JsonPinnedPlayer>, val pins: List<JsonPinData> = emptyList())

@Serializable
data class JsonPinnedPlayer(
    val id: String,
    val name: String = defaultPlayer.name,
    val email: String = defaultPlayer.email,
    val badge: String = "${defaultPlayer.badge}",
    val callSignAdjective: String = defaultPlayer.callSignAdjective,
    val callSignNoun: String = defaultPlayer.callSignNoun,
    val imageURL: String? = defaultPlayer.imageURL,
    val pins: List<JsonPinData>,
)

fun PairAssignmentDocument.toSerializable() = JsonPairAssignmentDocument(
    id = id.value,
    date = date.toDate().toISOString(),
    pairs = pairs.map(PinnedCouplingPair::toSerializable)
)

fun TribeRecord<PairAssignmentDocument>.toSerializable() = JsonPairAssignmentDocumentRecord(
    id = data.element.id.value,
    date = data.element.date.toDate().toISOString(),
    pairs = data.element.pairs.map(PinnedCouplingPair::toSerializable),
    tribeId = data.id.value,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp.toDate().toISOString(),
)

private fun PinnedCouplingPair.toSerializable() = JsonPinnedCouplingPair(
    players = players.map(PinnedPlayer::toSerializable),
    pins = pins.map(Pin::toSerializable)
)

private fun PinnedPlayer.toSerializable() = JsonPinnedPlayer(
    id = player.id,
    name = player.name,
    email = player.email,
    badge = "${player.badge}",
    callSignAdjective = player.callSignAdjective,
    callSignNoun = player.callSignNoun,
    imageURL = player.imageURL,
    pins = pins.map(Pin::toSerializable)
)

fun JsonPairAssignmentDocument.toModel() = PairAssignmentDocument(
    id = PairAssignmentDocumentId(id),
    date = DateTime.fromString(date).local,
    pairs = pairs.map(JsonPinnedCouplingPair::toModel)
)

fun JsonPairAssignmentDocumentRecord.toModel() = TribeRecord(
    TribeId(tribeId!!).with(PairAssignmentDocument(
        id = PairAssignmentDocumentId(id),
        date = DateTime.fromString(date).local,
        pairs = pairs.map(JsonPinnedCouplingPair::toModel)
    )
    ),
    modifyingUserId = modifyingUserEmail!!,
    isDeleted = isDeleted!!,
    timestamp = DateTime.fromString(timestamp!!).local
)

fun JsonPinnedCouplingPair.toModel() = PinnedCouplingPair(
    players = players.map(JsonPinnedPlayer::toModel),
    pins = pins.map(JsonPinData::toModel)
)

private fun JsonPinnedPlayer.toModel() = PinnedPlayer(
    player = Player(
        id = id,
        badge = badge.toIntOrNull() ?: defaultPlayer.badge,
        name = name,
        email = email,
        callSignAdjective = callSignAdjective,
        callSignNoun = callSignNoun,
        imageURL = imageURL
    ),
    pins = pins.map(JsonPinData::toModel)
)
