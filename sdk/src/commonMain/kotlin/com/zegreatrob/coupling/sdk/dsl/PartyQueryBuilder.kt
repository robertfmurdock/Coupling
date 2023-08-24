package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.JsonPair
import com.zegreatrob.coupling.json.JsonPairAssignment
import com.zegreatrob.coupling.json.JsonParty
import com.zegreatrob.coupling.sdk.dsl.GqlReference.contributionRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.integrationRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.pairAssignmentRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.partyRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.pinRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.pinnedCouplingPair
import com.zegreatrob.coupling.sdk.dsl.GqlReference.playerRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.secretRecord
import kotlinx.datetime.Instant
import kotools.types.collection.notEmptyListOf
import kotlin.time.Duration

class PartyQueryBuilder : QueryBuilder<JsonParty> {

    override var output: JsonParty = JsonParty("")

    fun pinList() = also { output = output.copy(pinList = listOf(pinRecord)) }
    fun details() = also { output = output.copy(details = partyRecord) }
    fun playerList() = also { output = output.copy(playerList = listOf(playerRecord)) }
    fun retiredPlayers() = also { output = output.copy(retiredPlayers = listOf(playerRecord)) }
    fun currentPairAssignments() = also { output = output.copy(currentPairAssignmentDocument = pairAssignmentRecord) }
    fun secretList() = also { output = output.copy(secretList = listOf(secretRecord)) }
    fun integration() = also { output = output.copy(integration = integrationRecord) }
    fun boost() = also { output = output.copy(boost = GqlReference.boost) }
    fun pairAssignmentDocumentList() = also {
        output = output.copy(pairAssignmentDocumentList = listOf(pairAssignmentRecord))
    }

    fun medianSpinDuration() = also { output = output.copy(medianSpinDuration = Duration.INFINITE) }
    fun spinsUntilFullRotation() = also { output = output.copy(spinsUntilFullRotation = Int.MAX_VALUE) }

    fun pairs(block: PairQueryBuilder.() -> Unit) = PairQueryBuilder()
        .also(block)
        .output
        .let { output = output.copy(pairs = listOf(it)) }

    fun contributions() = also { output = output.copy(contributions = listOf(contributionRecord)) }
}

class PairQueryBuilder : QueryBuilder<JsonPair> {
    override var output: JsonPair = JsonPair(null)

    fun players() = also { output = output.copy(players = listOf(playerRecord)) }
    fun count() = also { output = output.copy(count = 0) }
    fun heat() = also { output = output.copy(heat = 0.0) }
    fun spinsSinceLastPaired() = also { output = output.copy(spinsSinceLastPaired = 0) }
    fun pairAssignmentHistory(block: PairAssignmentQueryBuilder.() -> Unit) = PairAssignmentQueryBuilder()
        .also(block)
        .output
        .let { output = output.copy(pairAssignmentHistory = listOf(it)) }
}

class PairAssignmentQueryBuilder : QueryBuilder<JsonPairAssignment> {
    override var output: JsonPairAssignment = JsonPairAssignment(documentId = "")
    fun date() = also { output = output.copy(date = Instant.DISTANT_PAST) }
    fun pairs() = also { output = output.copy(allPairs = notEmptyListOf(pinnedCouplingPair)) }
    fun details() = also { output = output.copy(details = pairAssignmentRecord) }
}
