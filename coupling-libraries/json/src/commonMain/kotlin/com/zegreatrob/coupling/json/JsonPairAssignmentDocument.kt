@file:UseSerializers(DateTimeSerializer::class, TribeIdSerializer::class, PairAssignmentDocumentIdSerializer::class)

package com.zegreatrob.coupling.json

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class JsonPairAssignmentDocument(
    val id: PairAssignmentDocumentId,
    val date: DateTime,
    val pairs: List<JsonPinnedCouplingPair>
)

@Serializable
data class SpinOutput(val result: JsonPairAssignmentDocument)

@Serializable
data class JsonPairAssignmentDocumentRecord(
    val id: PairAssignmentDocumentId,
    val date: DateTime,
    val pairs: List<JsonPinnedCouplingPair>,
    override val tribeId: PartyId,
    override val modifyingUserEmail: String,
    override val isDeleted: Boolean,
    override val timestamp: DateTime
) : JsonTribeRecordInfo

@Serializable
data class SavePairAssignmentsInput(
    override val tribeId: PartyId,
    val pairAssignmentsId: PairAssignmentDocumentId,
    val date: DateTime,
    val pairs: List<JsonPinnedCouplingPair>,
) : TribeInput

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

@Serializable
data class SpinInput(
    override val tribeId: PartyId,
    val players: List<JsonPlayerData>,
    val pins: List<JsonPinData>,
) : TribeInput

fun PairAssignmentDocument.toSerializable() = JsonPairAssignmentDocument(
    id = id,
    date = date,
    pairs = pairs.map(PinnedCouplingPair::toSerializable)
)

fun PartyRecord<PairAssignmentDocument>.toSerializable() = JsonPairAssignmentDocumentRecord(
    id = data.element.id,
    date = data.element.date,
    pairs = data.element.pairs.map(PinnedCouplingPair::toSerializable),
    tribeId = data.id,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun PinnedCouplingPair.toSerializable() = JsonPinnedCouplingPair(
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

fun PartyElement<PairAssignmentDocument>.toSavePairAssignmentsInput() =
    SavePairAssignmentsInput(
        tribeId = partyId,
        pairAssignmentsId = element.id,
        date = element.date,
        pairs = element.pairs.map(PinnedCouplingPair::toSerializable),
    )

fun JsonPairAssignmentDocument.toModel() = PairAssignmentDocument(
    id = id,
    date = date,
    pairs = pairs.map(JsonPinnedCouplingPair::toModel)
)

fun SavePairAssignmentsInput.toModel() = PairAssignmentDocument(
    id = pairAssignmentsId,
    date = date,
    pairs = pairs.map(JsonPinnedCouplingPair::toModel)
)

fun JsonPairAssignmentDocumentRecord.toModel() = PartyRecord(
    tribeId.with(
        PairAssignmentDocument(
            id = id,
            date = date,
            pairs = pairs.map(JsonPinnedCouplingPair::toModel)
        )
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = date
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
