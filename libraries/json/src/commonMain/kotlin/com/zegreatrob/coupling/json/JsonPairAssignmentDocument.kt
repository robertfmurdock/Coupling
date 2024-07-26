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
import kotools.types.collection.toNotEmptyList

@Serializable
data class JsonPairAssignmentDocument(
    val id: String,
    val date: Instant,
    val pairs: NotEmptyList<GqlPinnedPair>,
    val discordMessageId: String? = null,
    val slackMessageId: String? = null,
)

@Serializable
data class JsonPairAssignment(
    val playerIds: List<String>? = null,
    val documentId: String? = null,
    val date: Instant? = null,
    val allPairs: NotEmptyList<GqlPinnedPair>? = null,
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
    allPairs = allPairs?.map(GqlPinnedPair::toModel),
    details = details?.toModel(),
    recentTimesPaired = recentTimesPaired,
)

@Serializable
data class JsonPairAssignmentDocumentRecord(
    val id: String,
    val date: Instant,
    val pairs: NotEmptyList<GqlPinnedPair>,
    val discordMessageId: String?,
    val slackMessageId: String?,
    override val partyId: PartyId,
    override val modifyingUserEmail: String,
    override val isDeleted: Boolean,
    override val timestamp: Instant,
) : JsonPartyRecordInfo

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

fun PinnedCouplingPair.toSerializable() = GqlPinnedPair(
    players = pinnedPlayers.map(PinnedPlayer::toSerializable).toList(),
    pins = pins.map(Pin::toSerializable),
)

fun PinnedCouplingPair.toSerializableInput() = GqlPinnedPairInput(
    players = pinnedPlayers.map(PinnedPlayer::toSerializableInput).toList(),
    pins = pins.map(Pin::toSerializableInput),
)

private fun PinnedPlayer.toSerializable() = GqlPinnedPlayer(
    id = player.id,
    name = player.name,
    email = player.email,
    badge = "${player.badge}",
    callSignAdjective = player.callSignAdjective,
    callSignNoun = player.callSignNoun,
    imageURL = player.imageURL,
    avatarType = player.avatarType?.toSerializable(),
    unvalidatedEmails = player.additionalEmails.toList(),
    pins = pins.map(Pin::toSerializable),
)

private fun PinnedPlayer.toSerializableInput() = GqlPinnedPlayerInput(
    id = player.id,
    name = player.name,
    email = player.email,
    badge = "${player.badge}",
    callSignAdjective = player.callSignAdjective,
    callSignNoun = player.callSignNoun,
    imageURL = player.imageURL,
    avatarType = player.avatarType?.toSerializable(),
    unvalidatedEmails = player.additionalEmails.toList(),
    pins = pins.map(Pin::toSerializableInput),
)

fun AvatarType.toSerializable() = name.let { GqlAvatarType.valueOfLabel(it) }

fun PartyElement<PairAssignmentDocument>.toSavePairAssignmentsInput() =
    GqlSavePairAssignmentsInput(
        partyId = partyId.value,
        pairAssignmentsId = element.id.value,
        date = element.date,
        pairs = element.pairs.map(PinnedCouplingPair::toSerializableInput).toList(),
        discordMessageId = element.discordMessageId,
        slackMessageId = element.slackMessageId,
    )

fun JsonPairAssignmentDocument.toModel() = PairAssignmentDocument(
    id = id.let(::PairAssignmentDocumentId),
    date = date,
    pairs = pairs.map(GqlPinnedPair::toModel),
    discordMessageId = discordMessageId,
    slackMessageId = slackMessageId,
)

fun GqlSavePairAssignmentsInput.toModel() = PairAssignmentDocument(
    id = pairAssignmentsId.let(::PairAssignmentDocumentId),
    date = date,
    pairs = pairs.map(GqlPinnedPairInput::toModel).toNotEmptyList().getOrThrow(),
    discordMessageId = discordMessageId,
    slackMessageId = slackMessageId,
)

fun JsonPairAssignmentDocumentRecord.toModel(): PartyRecord<PairAssignmentDocument> = PartyRecord(
    partyId.with(
        PairAssignmentDocument(
            id = id.let(::PairAssignmentDocumentId),
            date = date,
            pairs = pairs.map(GqlPinnedPair::toModel),
            discordMessageId = discordMessageId,
            slackMessageId = slackMessageId,
        ),
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = date,
)

fun GqlPinnedPair.toModel() = PinnedCouplingPair(
    pinnedPlayers = players.map(GqlPinnedPlayer::toModel).toNotEmptyList().getOrThrow(),
    pins = pins.map(GqlPin::toModel).toSet(),
)

fun GqlPinnedPairInput.toModel() = PinnedCouplingPair(
    pinnedPlayers = players.map(GqlPinnedPlayerInput::toModel).toNotEmptyList().getOrThrow(),
    pins = pins.map(GqlPinInput::toModel).toSet(),
)

private fun GqlPinnedPlayer.toModel() = PinnedPlayer(
    player = Player(
        id = id,
        badge = badge?.toIntOrNull() ?: defaultPlayer.badge,
        name = name,
        email = email,
        callSignAdjective = callSignAdjective,
        callSignNoun = callSignNoun,
        imageURL = imageURL,
        avatarType = avatarType?.toModel(),
        additionalEmails = unvalidatedEmails?.toSet() ?: emptySet(),
    ),
    pins = pins.map(GqlPin::toModel),
)

private fun GqlPinnedPlayerInput.toModel() = PinnedPlayer(
    player = Player(
        id = id,
        badge = badge?.toIntOrNull() ?: defaultPlayer.badge,
        name = name,
        email = email,
        callSignAdjective = callSignAdjective,
        callSignNoun = callSignNoun,
        imageURL = imageURL,
        avatarType = avatarType?.toModel(),
        additionalEmails = unvalidatedEmails.toSet(),
    ),
    pins = pins.map(GqlPinInput::toModel),
)

private fun GqlAvatarType.toModel() = name.let { AvatarType.valueOf(it) }
