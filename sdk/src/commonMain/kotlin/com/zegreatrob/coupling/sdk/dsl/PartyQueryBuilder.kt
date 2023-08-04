package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.JsonParty
import com.zegreatrob.coupling.sdk.dsl.GqlReference.integrationRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.pairAssignmentRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.partyRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.pinRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.playerRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.secretRecord

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
}
