package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignment
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import kotools.types.collection.toNotEmptyList
import org.kotools.types.ExperimentalKotoolsTypesApi

fun PairAssignment.toSerializable() = GqlPairAssignment(
    playerIds = playerIds,
    documentId = documentId,
    date = date,
    allPairs = allPairs?.map(PinnedCouplingPair::toSerializable)?.toList(),
    details = details?.toSerializable(),
    recentTimesPaired = recentTimesPaired,
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlPairAssignment.toModel() = PairAssignment(
    playerIds = playerIds,
    documentId = documentId,
    date = date,
    allPairs = allPairs?.map(GqlPinnedPair::toModel)?.toNotEmptyList()?.getOrNull(),
    details = details?.toModel(),
    recentTimesPaired = recentTimesPaired,
)
