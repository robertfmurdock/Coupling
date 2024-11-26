package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.party.PartyElement
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
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

fun PartyElement<PairAssignmentDocument>.toSavePairAssignmentsInput() = GqlSavePairAssignmentsInput(
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
