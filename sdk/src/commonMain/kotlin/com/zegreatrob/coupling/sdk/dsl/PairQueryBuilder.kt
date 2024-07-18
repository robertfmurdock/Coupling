package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.ContributionsInput
import com.zegreatrob.coupling.json.JsonContributionWindow
import com.zegreatrob.coupling.json.JsonContributor
import com.zegreatrob.coupling.json.JsonPair
import kotlinx.serialization.json.JsonElement

class PairQueryBuilder :
    BuilderWithInput(),
    QueryBuilder<JsonPair> {
    override var output: JsonPair = JsonPair(null)
    override var queries = mutableListOf<String>()
    override var inputs = mutableListOf<String>()
    override var variables = mutableMapOf<String, JsonElement>()

    fun players() = also { output = output.copy(players = listOf(GqlReference.playerRecord)) }
    fun count() = also { output = output.copy(count = 0) }
    fun recentTimesPaired() = also { output = output.copy(recentTimesPaired = 0) }
    fun spinsSinceLastPaired() = also { output = output.copy(spinsSinceLastPaired = 0) }
    fun contributions(window: JsonContributionWindow? = null, limit: Int? = null) = also {
        if (window == null) {
            output = output.copy(contributions = listOf(GqlReference.contributionRecord))
        } else {
            GqlReference.contributionRecord.addToQuery(
                queryKey = "contributions",
                inputSettings = InputSettings(
                    ContributionsInput(window, limit),
                    "contributionsInput",
                    "ContributionsInput",
                ),
            )
        }
    }

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
