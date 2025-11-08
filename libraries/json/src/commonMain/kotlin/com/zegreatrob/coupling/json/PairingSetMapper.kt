package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.party.with
import kotlinx.serialization.Serializable
import kotools.types.collection.NotEmptyList
import kotools.types.collection.toNotEmptyList
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.time.Instant

fun PartyRecord<PairingSet>.toSerializable() = GqlPairingSet(
    partyId = data.partyId,
    id = data.element.id,
    date = data.element.date,
    pairs = data.element.pairs.map(PinnedCouplingPair::toSerializable).toList(),
    discordMessageId = data.element.discordMessageId,
    slackMessageId = data.element.slackMessageId,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
    recentTimesPaired = null,
)

fun PairingSet.toSerializable() = JsonPairingSet(
    id = id,
    date = date,
    pairs = pairs.map(PinnedCouplingPair::toSerializable),
    discordMessageId = discordMessageId,
    slackMessageId = slackMessageId,
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlPairingSet.toModel(): PartyRecord<PairingSet> = PartyRecord(
    partyId.with(
        PairingSet(
            id = id,
            date = date,
            pairs = pairs.map(GqlPairSnapshot::toModel).toNotEmptyList().getOrThrow(),
            discordMessageId = discordMessageId,
            slackMessageId = slackMessageId,
        ),
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = date,
)

@Serializable
data class JsonPairingSet(
    val id: PairingSetIdString,
    val date: Instant,
    val pairs: NotEmptyList<GqlPairSnapshot>,
    val discordMessageId: String? = null,
    val slackMessageId: String? = null,
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun JsonPairingSet.toModel() = PairingSet(
    id = id,
    date = date,
    pairs = pairs.map(GqlPairSnapshot::toModel),
    discordMessageId = discordMessageId,
    slackMessageId = slackMessageId,
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlSavePairAssignmentsInput.toModel() = PairingSet(
    id = pairingSetId,
    date = date,
    pairs = pairs.map(GqlPinnedPairInput::toModel).toNotEmptyList().getOrThrow(),
    discordMessageId = discordMessageId,
    slackMessageId = slackMessageId,
)
