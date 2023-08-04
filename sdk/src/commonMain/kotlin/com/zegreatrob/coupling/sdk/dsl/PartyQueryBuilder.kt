package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.JsonBoostRecord
import com.zegreatrob.coupling.json.JsonParty
import com.zegreatrob.coupling.json.JsonUser
import com.zegreatrob.coupling.sdk.dsl.GqlReference.integrationRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.pairAssignmentRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.partyRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.pinRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.playerRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.secretRecord
import com.zegreatrob.coupling.sdk.dsl.GqlReference.user
import kotlinx.datetime.Instant

class PartyQueryBuilder : QueryBuilder<JsonParty> {

    override var output: JsonParty = JsonParty("")

    fun pinList() {
        output = output.copy(
            pinList = listOf(pinRecord),
        )
    }

    fun details() {
        output = output.copy(
            details = partyRecord,
        )
    }

    fun playerList() {
        output = output.copy(
            playerList = listOf(playerRecord),
        )
    }

    fun retiredPlayers() {
        output = output.copy(
            retiredPlayers = listOf(playerRecord),
        )
    }

    fun currentPairAssignments() {
        output = output.copy(
            currentPairAssignmentDocument = pairAssignmentRecord,
        )
    }

    fun pairAssignmentDocumentList() {
        output = output.copy(
            pairAssignmentDocumentList = listOf(pairAssignmentRecord),
        )
    }

    fun secretList() {
        output = output.copy(
            secretList = listOf(secretRecord),
        )
    }

    fun integration() {
        output = output.copy(
            integration = integrationRecord,
        )
    }
}

class UserQueryBuilder : QueryBuilder<JsonUser> {
    override var output: JsonUser = JsonUser("", null, null)

    fun details() {
        output = output.copy(details = user)
    }

    fun boost() {
        output = output.copy(boost = JsonBoostRecord("", emptySet(), "", false, Instant.DISTANT_FUTURE))
    }
}
