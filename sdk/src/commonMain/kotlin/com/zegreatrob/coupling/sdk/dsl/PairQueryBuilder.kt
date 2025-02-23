package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.json.GqlContributionsInput
import com.zegreatrob.coupling.json.GqlContributor
import com.zegreatrob.coupling.json.GqlPair
import kotlinx.serialization.json.JsonElement

class PairQueryBuilder :
    BuilderWithInput(),
    QueryBuilder<GqlPair> {
    override var output: GqlPair = GqlPair(
        contributionReport = null,
        count = null,
        pairAssignmentHistory = null,
        partyId = null,
        players = null,
        recentTimesPaired = null,
        spinsSinceLastPaired = null,
    )
    override var queries = mutableListOf<String>()
    override var inputs = mutableListOf<String>()
    override var variables = mutableMapOf<String, JsonElement>()

    fun players() = also { output = output.copy(players = listOf(GqlReference.playerRecord)) }
    fun count() = also { output = output.copy(count = 0) }
    fun recentTimesPaired() = also { output = output.copy(recentTimesPaired = 0) }
    fun spinsSinceLastPaired() = also { output = output.copy(spinsSinceLastPaired = 0) }
    fun contributionReport(
        window: GqlContributionWindow? = null,
        limit: Int? = null,
        block: ContributionReportBuilder.() -> Unit,
    ) = ContributionReportBuilder()
        .also(block)
        .output
        .addToQuery(
            queryKey = "contributionReport",
            inputSettings = InputSettings(
                GqlContributionsInput(limit = limit, window = window),
                "contributionsInput",
                "ContributionsInput",
            ),
        )

    fun pairAssignmentHistory(block: PairAssignmentQueryBuilder.() -> Unit) = PairAssignmentQueryBuilder()
        .also(block)
        .output
        .let { output = output.copy(pairAssignmentHistory = listOf(it)) }
}

class ContributorQueryBuilder : QueryBuilder<GqlContributor> {
    override var output: GqlContributor = GqlContributor(playerId = null, email = null)
    fun email() = also { output = output.copy(email = "") }
    fun playerId() = also { output = output.copy(playerId = "") }
}
