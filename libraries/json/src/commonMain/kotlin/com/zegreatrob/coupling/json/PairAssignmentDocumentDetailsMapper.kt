package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import kotools.types.collection.toNotEmptyList

fun PartyRecord<PairAssignmentDocument>.toSerializable() = GqlPairAssignmentDocumentDetails(
    partyId = data.partyId.value,
    id = data.element.id.value,
    date = data.element.date,
    pairs = data.element.pairs.map(PinnedCouplingPair::toSerializable).toList(),
    discordMessageId = data.element.discordMessageId,
    slackMessageId = data.element.slackMessageId,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun PairAssignmentDocument.toSerializable() = JsonPairAssignmentDocument(
    id = id.value,
    date = date,
    pairs = pairs.map(PinnedCouplingPair::toSerializable),
    discordMessageId = discordMessageId,
    slackMessageId = slackMessageId,
)

fun GqlPairAssignmentDocumentDetails.toModel(): PartyRecord<PairAssignmentDocument> =
    PartyRecord(
        PartyId(partyId).with(
            PairAssignmentDocument(
                id = id.let(::PairAssignmentDocumentId),
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
