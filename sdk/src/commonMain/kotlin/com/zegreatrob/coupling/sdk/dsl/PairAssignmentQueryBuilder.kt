package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.GqlPairAssignment
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import kotools.types.text.toNotBlankString
import kotlin.time.Instant

class PairAssignmentQueryBuilder : QueryBuilder<GqlPairAssignment> {
    override var output: GqlPairAssignment = GqlPairAssignment(
        documentId = PairAssignmentDocumentId("-".toNotBlankString().getOrThrow()),
        allPairs = null,
        date = null,
        details = null,
        playerIds = null,
        recentTimesPaired = null,
    )

    fun date() = also { output = output.copy(date = Instant.DISTANT_PAST) }
    fun recentTimesPaired() = also { output = output.copy(recentTimesPaired = Int.MAX_VALUE) }
    fun pairs() = also { output = output.copy(allPairs = listOf(GqlReference.pinnedCouplingPair)) }
    fun details() = also { output = output.copy(details = GqlReference.pairAssignmentRecord) }
}
