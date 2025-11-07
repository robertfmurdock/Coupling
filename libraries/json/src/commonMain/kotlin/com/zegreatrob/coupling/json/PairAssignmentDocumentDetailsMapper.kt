package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.party.with
import kotools.types.collection.toNotEmptyList
import org.kotools.types.ExperimentalKotoolsTypesApi

fun PartyRecord<PairAssignmentDocument>.toSerializable() = GqlPairingSet(
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

fun PairAssignmentDocument.toSerializable() = JsonPairAssignmentDocument(
    id = id,
    date = date,
    pairs = pairs.map(PinnedCouplingPair::toSerializable),
    discordMessageId = discordMessageId,
    slackMessageId = slackMessageId,
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlPairingSet.toModel(): PartyRecord<PairAssignmentDocument> = PartyRecord(
    partyId.with(
        PairAssignmentDocument(
            id = id,
            date = date,
            pairs = pairs.map(GqlPinnedPair::toModel).toNotEmptyList().getOrThrow(),
            discordMessageId = discordMessageId,
            slackMessageId = slackMessageId,
        ),
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = date,
)
