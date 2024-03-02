@file:UseSerializers(PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignment
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotools.types.collection.NotEmptyList

@Serializable
data class JsonPairAssignmentDocument(
    val id: String,
    val date: Instant,
    val pairs: NotEmptyList<JsonPinnedCouplingPair>,
    val discordMessageId: String? = null,
    val slackMessageId: String? = null,
)

@Serializable
data class JsonPairAssignment(
    val playerIds: List<String>? = null,
    val documentId: String? = null,
    val date: Instant? = null,
    val allPairs: NotEmptyList<JsonPinnedCouplingPair>? = null,
    val details: JsonPairAssignmentDocumentRecord? = null,
    val recentTimesPaired: Int? = null,
)

fun PairAssignment.toSerializable() = JsonPairAssignment(
    playerIds = playerIds,
    documentId = documentId?.value,
    date = date,
    allPairs = allPairs?.map(PinnedCouplingPair::toSerializable),
    details = details?.toSerializable(),
    recentTimesPaired = recentTimesPaired,
)

fun JsonPairAssignment.toModel() = PairAssignment(
    playerIds = playerIds,
    documentId = documentId?.let(::PairAssignmentDocumentId),
    date = date,
    allPairs = allPairs?.map(JsonPinnedCouplingPair::toModel),
    details = details?.toModel(),
    recentTimesPaired = recentTimesPaired,
)

@Serializable
data class JsonPairAssignmentDocumentRecord(
    val id: String,
    val date: Instant,
    val pairs: NotEmptyList<JsonPinnedCouplingPair>,
    val discordMessageId: String?,
    val slackMessageId: String?,
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
data class SavePairAssignmentsInput(
    override val partyId: PartyId,
    val pairAssignmentsId: String,
    val date: Instant,
    val pairs: NotEmptyList<JsonPinnedCouplingPair>,
    val discordMessageId: String?,
    val slackMessageId: String?,
) : IPartyInput

@Serializable
data class JsonPinnedCouplingPair(val players: NotEmptyList<JsonPinnedPlayer>, val pins: Set<JsonPinData> = emptySet())

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
    val pins: List<JsonPinData> = emptyList(),
    val unvalidatedEmails: Set<String> = emptySet(),
)

@Serializable
data class SpinInput(
    override val partyId: PartyId,
    val playerIds: NotEmptyList<String>,
    val pinIds: List<String>,
) : IPartyInput

@Serializable
data class GrantSlackAccessInput(
    val code: String,
    val state: String,
)

@Serializable
data class GrantDiscordAccessInput(
    val code: String,
    val partyId: String,
    val guildId: String,
)

fun PairAssignmentDocument.toSerializable() = JsonPairAssignmentDocument(
    id = id.value,
    date = date,
    pairs = pairs.map(PinnedCouplingPair::toSerializable),
    discordMessageId = discordMessageId,
    slackMessageId = slackMessageId,
)

fun PartyRecord<PairAssignmentDocument>.toSerializable() = JsonPairAssignmentDocumentRecord(
    partyId = data.partyId,
    id = data.element.id.value,
    date = data.element.date,
    pairs = data.element.pairs.map(PinnedCouplingPair::toSerializable),
    discordMessageId = data.element.discordMessageId,
    slackMessageId = data.element.slackMessageId,
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
    unvalidatedEmails = player.additionalEmails,
    pins = pins.map(Pin::toSerializable),
)

fun PartyElement<PairAssignmentDocument>.toSavePairAssignmentsInput() =
    SavePairAssignmentsInput(
        partyId = partyId,
        pairAssignmentsId = element.id.value,
        date = element.date,
        pairs = element.pairs.map(PinnedCouplingPair::toSerializable),
        discordMessageId = element.discordMessageId,
        slackMessageId = element.slackMessageId,
    )

fun JsonPairAssignmentDocument.toModel() = PairAssignmentDocument(
    id = id.let(::PairAssignmentDocumentId),
    date = date,
    pairs = pairs.map(JsonPinnedCouplingPair::toModel),
    discordMessageId = discordMessageId,
    slackMessageId = slackMessageId,
)

fun SavePairAssignmentsInput.toModel() = PairAssignmentDocument(
    id = pairAssignmentsId.let(::PairAssignmentDocumentId),
    date = date,
    pairs = pairs.map(JsonPinnedCouplingPair::toModel),
    discordMessageId = discordMessageId,
    slackMessageId = slackMessageId,
)

fun JsonPairAssignmentDocumentRecord.toModel(): PartyRecord<PairAssignmentDocument> {
    return PartyRecord(
        partyId.with(
            PairAssignmentDocument(
                id = id.let(::PairAssignmentDocumentId),
                date = date,
                pairs = pairs.map(JsonPinnedCouplingPair::toModel),
                discordMessageId = discordMessageId,
                slackMessageId = slackMessageId,
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
        additionalEmails = unvalidatedEmails,
    ),
    pins = pins.map(JsonPinData::toModel),
)
