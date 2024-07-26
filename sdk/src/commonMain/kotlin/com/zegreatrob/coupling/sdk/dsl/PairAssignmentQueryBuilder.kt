package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.GqlPairAssignment
import kotlinx.datetime.Instant

class PairAssignmentQueryBuilder : QueryBuilder<GqlPairAssignment> {
    override var output: GqlPairAssignment = GqlPairAssignment(
        documentId = "",
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
