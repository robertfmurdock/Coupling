package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignment
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.player.PlayerId
import kotools.types.collection.toNotEmptyList
import kotools.types.text.NotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi

fun PairAssignment.toSerializable() = GqlPairAssignment(
    playerIds = playerIds?.map { it.value.toString() },
    documentId = documentId?.value?.toString(),
    date = date,
    allPairs = allPairs?.map(PinnedCouplingPair::toSerializable)?.toList(),
    details = details?.toSerializable(),
    recentTimesPaired = recentTimesPaired,
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlPairAssignment.toModel() = PairAssignment(
    playerIds = playerIds?.map { PlayerId(NotBlankString.create(it)) },
    documentId = documentId?.let { PairAssignmentDocumentId(NotBlankString.create(it)) },
    date = date,
    allPairs = allPairs?.map(GqlPinnedPair::toModel)?.toNotEmptyList()?.getOrNull(),
    details = details?.toModel(),
    recentTimesPaired = recentTimesPaired,
)
