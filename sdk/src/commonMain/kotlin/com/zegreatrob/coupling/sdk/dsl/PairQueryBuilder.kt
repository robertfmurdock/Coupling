package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.JsonContributor
import com.zegreatrob.coupling.json.JsonPair

class PairQueryBuilder : QueryBuilder<JsonPair> {
    override var output: JsonPair = JsonPair(null)

    fun players() = also { output = output.copy(players = listOf(GqlReference.playerRecord)) }
    fun count() = also { output = output.copy(count = 0) }
    fun recentTimesPaired() = also { output = output.copy(recentTimesPaired = 0) }
    fun spinsSinceLastPaired() = also { output = output.copy(spinsSinceLastPaired = 0) }
    fun contributions() = also { output = output.copy(contributions = listOf(GqlReference.contributionRecord)) }
    fun pairAssignmentHistory(block: PairAssignmentQueryBuilder.() -> Unit) = PairAssignmentQueryBuilder()
        .also(block)
        .output
        .let { output = output.copy(pairAssignmentHistory = listOf(it)) }
}

class ContributorQueryBuilder : QueryBuilder<JsonContributor> {
    override var output: JsonContributor = JsonContributor()
    fun email() = also { output = output.copy(email = "") }
    fun details() = also { output = output.copy(details = GqlReference.playerRecord) }
}
