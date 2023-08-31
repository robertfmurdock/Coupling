package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.JsonPairAssignment
import kotlinx.datetime.Instant
import kotools.types.collection.notEmptyListOf

class PairAssignmentQueryBuilder : QueryBuilder<JsonPairAssignment> {
    override var output: JsonPairAssignment = JsonPairAssignment(documentId = "")
    fun date() = also { output = output.copy(date = Instant.DISTANT_PAST) }
    fun recentTimesPaired() = also { output = output.copy(recentTimesPaired = Int.MAX_VALUE) }
    fun pairs() = also { output = output.copy(allPairs = notEmptyListOf(GqlReference.pinnedCouplingPair)) }
    fun details() = also { output = output.copy(details = GqlReference.pairAssignmentRecord) }
}
