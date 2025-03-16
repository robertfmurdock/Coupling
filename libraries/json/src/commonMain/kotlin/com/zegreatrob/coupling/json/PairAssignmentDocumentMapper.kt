package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.party.PartyElement
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotools.types.collection.NotEmptyList
import kotools.types.collection.toNotEmptyList
import org.kotools.types.ExperimentalKotoolsTypesApi

@Serializable
data class JsonPairAssignmentDocument(
    val id: PairAssignmentDocumentIdString,
    val date: Instant,
    val pairs: NotEmptyList<GqlPinnedPair>,
    val discordMessageId: String? = null,
    val slackMessageId: String? = null,
)

fun PartyElement<PairAssignmentDocument>.toSavePairAssignmentsInput() = GqlSavePairAssignmentsInput(
    partyId = partyId,
    pairAssignmentsId = element.id,
    date = element.date,
    pairs = element.pairs.map(PinnedCouplingPair::toSerializableInput).toList(),
    discordMessageId = element.discordMessageId,
    slackMessageId = element.slackMessageId,
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun JsonPairAssignmentDocument.toModel() = PairAssignmentDocument(
    id = id,
    date = date,
    pairs = pairs.map(GqlPinnedPair::toModel),
    discordMessageId = discordMessageId,
    slackMessageId = slackMessageId,
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlSavePairAssignmentsInput.toModel() = PairAssignmentDocument(
    id = pairAssignmentsId,
    date = date,
    pairs = pairs.map(GqlPinnedPairInput::toModel).toNotEmptyList().getOrThrow(),
    discordMessageId = discordMessageId,
    slackMessageId = slackMessageId,
)
