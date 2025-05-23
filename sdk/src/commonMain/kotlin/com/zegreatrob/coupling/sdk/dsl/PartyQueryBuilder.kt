package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.GqlContributionReport
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.json.GqlContributionsInput
import com.zegreatrob.coupling.json.GqlPairInput
import com.zegreatrob.coupling.json.GqlPairsInput
import com.zegreatrob.coupling.json.GqlParty
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.sdk.dsl.GqlReference.boost
import com.zegreatrob.coupling.sdk.dsl.GqlReference.contributionRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.integrationRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.pairAssignmentRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.partyRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.pinRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.playerRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.secretRecord
import kotlinx.serialization.json.JsonElement
import kotlin.time.Duration

class PartyQueryBuilder :
    BuilderWithInput(),
    QueryBuilder<GqlParty> {
    override var output: GqlParty = GqlParty(
        id = PartyId("-"),
        boost = null,
        contributionReport = null,
        currentPairAssignmentDocument = null,
        details = null,
        integration = null,
        medianSpinDuration = null,
        pair = null,
        pairAssignmentDocumentList = null,
        pairs = null,
        pinList = null,
        playerList = null,
        retiredPlayers = null,
        secretList = null,
        spinsUntilFullRotation = null,
    )

    override var queries = mutableListOf<String>()
    override var inputs = mutableListOf<String>()
    override var variables = mutableMapOf<String, JsonElement>()

    fun boost() = also { output = output.copy(boost = boost) }

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
                GqlContributionsInput(window = window, limit = limit),
                "contributionsInput",
                "ContributionsInput",
            ),
        )

    fun currentPairAssignments() = also { output = output.copy(currentPairAssignmentDocument = pairAssignmentRecord) }
    fun details() = also { output = output.copy(details = partyRecord) }
    fun integration() = also { output = output.copy(integration = integrationRecord) }
    fun pairAssignmentDocumentList() = also {
        output = output.copy(pairAssignmentDocumentList = listOf(pairAssignmentRecord))
    }

    fun pair(vararg playerIds: PlayerId, block: PairQueryBuilder.() -> Unit) = PairQueryBuilder()
        .also(block)
        .output
        .addToQuery(
            "pair",
            InputSettings(GqlPairInput(playerIds.toList()), "pairInput", "PairInput"),
        )

    fun pairs(includeRetired: Boolean? = true, block: PairQueryBuilder.() -> Unit) = PairQueryBuilder()
        .also(block)
        .also {
            mergeToParent(
                "pairs",
                inputSettings = InputSettings(GqlPairsInput(includeRetired), "pairsInput", "PairsInput"),
                child = it,
            )
        }

    fun pinList() = also { output = output.copy(pinList = listOf(pinRecord)) }
    fun playerList() = also { output = output.copy(playerList = listOf(playerRecord)) }
    fun retiredPlayers() = also { output = output.copy(retiredPlayers = listOf(playerRecord)) }
    fun secretList() = also { output = output.copy(secretList = listOf(secretRecord)) }
    fun medianSpinDuration() = also { output = output.copy(medianSpinDuration = Duration.INFINITE) }
    fun spinsUntilFullRotation() = also { output = output.copy(spinsUntilFullRotation = Int.MAX_VALUE) }
}

class ContributionReportBuilder :
    BuilderWithInput(),
    QueryBuilder<GqlContributionReport> {
    override var output: GqlContributionReport =
        GqlContributionReport(
            partyId = null,
            contributions = null,
            contributors = null,
            withCycleTimeCount = null,
            count = null,
            medianCycleTime = null,
        )
    override var queries = mutableListOf<String>()
    override var inputs = mutableListOf<String>()
    override var variables = mutableMapOf<String, JsonElement>()

    fun count() {
        also { output = output.copy(count = 0) }
    }

    fun medianCycleTime() {
        also { output = output.copy(medianCycleTime = Duration.INFINITE) }
    }

    fun withCycleTimeCount() {
        also { output = output.copy(withCycleTimeCount = 0) }
    }

    fun contributors(block: ContributorQueryBuilder.() -> Unit) = ContributorQueryBuilder()
        .also(block)
        .output
        .let { output = output.copy(contributors = listOf(it)) }

    fun contributions() = also {
        also { output = output.copy(contributions = listOf(contributionRecord)) }
    }
}
