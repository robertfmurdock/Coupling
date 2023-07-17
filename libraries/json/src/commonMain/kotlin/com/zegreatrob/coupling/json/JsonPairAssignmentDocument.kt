@file:UseSerializers(DateTimeSerializer::class, PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotools.types.collection.toNotEmptyList

@Serializable
data class JsonPairAssignmentDocument(
    val id: String,
    val date: Instant,
    val pairs: List<JsonPinnedCouplingPair>,
)

@Serializable
data class JsonPairAssignmentDocumentRecord(
    val id: String,
    val date: Instant,
    val pairs: List<JsonPinnedCouplingPair>,
    override val partyId: PartyId,
    override val modifyingUserEmail: String,
    override val isDeleted: Boolean,
    override val timestamp: Instant,
) : JsonPartyRecordInfo

@Serializable
data class JsonIntegrationRecord(
    val slackTeam: String?,
    val slackChannel: String?,
    val modifyingUserEmail: String,
    val isDeleted: Boolean,
    val timestamp: Instant,
)

@Serializable
data class JsonSecretRecord(
    val id: String,
    override val partyId: PartyId,
    override val modifyingUserEmail: String,
    override val isDeleted: Boolean,
    override val timestamp: Instant,
) : JsonPartyRecordInfo

fun JsonSecretRecord.toModel(): PartyRecord<Secret> = PartyRecord(
    partyId.with(Secret(id = id)),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun PartyRecord<Secret>.toSerializable() = JsonSecretRecord(
    id = data.element.id,
    partyId = data.partyId,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

@Serializable
data class SavePairAssignmentsInput(
    override val partyId: PartyId,
    val pairAssignmentsId: String,
    val date: Instant,
    val pairs: List<JsonPinnedCouplingPair>,
) : IPartyInput

@Serializable
data class JsonPinnedCouplingPair(val players: List<JsonPinnedPlayer>, val pins: Set<JsonPinData> = emptySet())

@Serializable
data class JsonPinnedPlayer(
    val id: String,
    val name: String = defaultPlayer.name,
    val email: String = defaultPlayer.email,
    val badge: String = "${defaultPlayer.badge}",
    val callSignAdjective: String = defaultPlayer.callSignAdjective,
    val callSignNoun: String = defaultPlayer.callSignNoun,
    val imageURL: String? = defaultPlayer.imageURL,
    val avatarType: AvatarType? = defaultPlayer.avatarType,
    val pins: List<JsonPinData>,
)

@Serializable
data class SpinInput(
    override val partyId: PartyId,
    val playerIds: List<String>,
    val pinIds: List<String>,
) : IPartyInput

@Serializable
data class GrantSlackAccessInput(
    val code: String,
    val state: String,
)

fun PairAssignmentDocument.toSerializable() = JsonPairAssignmentDocument(
    id = id.value,
    date = date,
    pairs = pairs.toList().map(PinnedCouplingPair::toSerializable),
)

fun PartyRecord<PairAssignmentDocument>.toSerializable() = JsonPairAssignmentDocumentRecord(
    id = data.element.id.value,
    date = data.element.date,
    pairs = data.element.pairs.toList().map(PinnedCouplingPair::toSerializable),
    partyId = data.partyId,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun PinnedCouplingPair.toSerializable() = JsonPinnedCouplingPair(
    players = pinnedPlayers.map(PinnedPlayer::toSerializable),
    pins = pins.map(Pin::toSerializable).toSet(),
)

private fun PinnedPlayer.toSerializable() = JsonPinnedPlayer(
    id = player.id,
    name = player.name,
    email = player.email,
    badge = "${player.badge}",
    callSignAdjective = player.callSignAdjective,
    callSignNoun = player.callSignNoun,
    imageURL = player.imageURL,
    avatarType = player.avatarType,
    pins = pins.map(Pin::toSerializable),
)

fun PartyElement<PairAssignmentDocument>.toSavePairAssignmentsInput() =
    SavePairAssignmentsInput(
        partyId = partyId,
        pairAssignmentsId = element.id.value,
        date = element.date,
        pairs = element.pairs.toList().map(PinnedCouplingPair::toSerializable),
    )

fun JsonPairAssignmentDocument.toModel() = PairAssignmentDocument(
    id = id.let(::PairAssignmentDocumentId),
    date = date,
    pairs = pairs.map(JsonPinnedCouplingPair::toModel).toNotEmptyList().getOrThrow(),
)

fun SavePairAssignmentsInput.toModel() = PairAssignmentDocument(
    id = pairAssignmentsId.let(::PairAssignmentDocumentId),
    date = date,
    pairs = pairs.map(JsonPinnedCouplingPair::toModel).toNotEmptyList().getOrThrow(),
)

fun JsonPairAssignmentDocumentRecord.toModel(): PartyRecord<PairAssignmentDocument>? {
    return PartyRecord(
        partyId.with(
            PairAssignmentDocument(
                id = id.let(::PairAssignmentDocumentId),
                date = date,
                pairs = pairs.map(JsonPinnedCouplingPair::toModel).toNotEmptyList().getOrNull() ?: return null,
            ),
        ),
        modifyingUserId = modifyingUserEmail,
        isDeleted = isDeleted,
        timestamp = date,
    )
}

fun JsonPinnedCouplingPair.toModel() = PinnedCouplingPair(
    pinnedPlayers = players.map(JsonPinnedPlayer::toModel),
    pins = pins.map(JsonPinData::toModel).toSet(),
)

private fun JsonPinnedPlayer.toModel() = PinnedPlayer(
    player = Player(
        id = id,
        badge = badge.toIntOrNull() ?: defaultPlayer.badge,
        name = name,
        email = email,
        callSignAdjective = callSignAdjective,
        callSignNoun = callSignNoun,
        imageURL = imageURL,
        avatarType = avatarType,
    ),
    pins = pins.map(JsonPinData::toModel),
)
