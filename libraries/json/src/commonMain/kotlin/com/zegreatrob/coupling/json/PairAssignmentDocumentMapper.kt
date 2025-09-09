package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.party.PartyElement
import kotlinx.serialization.Serializable
import kotools.types.collection.NotEmptyList
import kotools.types.collection.toNotEmptyList
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.time.Instant

@Serializable
data class JsonPairAssignmentDocument(
    val id: PairAssignmentDocumentIdString,
    val date: Instant,
    val pairs: NotEmptyList<GqlPinnedPair>,
    val discordMessageId: String? = null,
    val slackMessageId: String? = null,
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
