package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignment
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import kotools.types.collection.toNotEmptyList

fun PairAssignment.toSerializable() = GqlPairAssignment(
    playerIds = playerIds,
    documentId = documentId?.value,
    date = date,
    allPairs = allPairs?.map(PinnedCouplingPair::toSerializable)?.toList(),
    details = details?.toSerializable(),
    recentTimesPaired = recentTimesPaired,
)

fun GqlPairAssignment.toModel() = PairAssignment(
    playerIds = playerIds,
    documentId = documentId?.let(::PairAssignmentDocumentId),
    date = date,
    allPairs = allPairs?.map(GqlPinnedPair::toModel)?.toNotEmptyList()?.getOrNull(),
    details = details?.toModel(),
    recentTimesPaired = recentTimesPaired,
)
