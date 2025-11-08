package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import kotlinx.serialization.Serializable
import kotools.types.collection.NotEmptyList
import kotools.types.collection.toNotEmptyList
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.time.Instant

@Serializable
data class JsonPairingSet(
    val id: PairingSetIdString,
    val date: Instant,
    val pairs: NotEmptyList<GqlPinnedPair>,
    val discordMessageId: String? = null,
    val slackMessageId: String? = null,
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun JsonPairingSet.toModel() = PairingSet(
    id = id,
    date = date,
    pairs = pairs.map(GqlPinnedPair::toModel),
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
