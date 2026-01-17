package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.test
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SaveContributionCommandWrapper
import com.zegreatrob.coupling.cli.cli
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.PartyDetailsQuery
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.testaction.StubCannon
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class DetailsCLITest {

    @Test
    fun willReturnPartyDetailsFromQuery() = asyncSetup(object : ScopeMint() {
        val partyId = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also { it.givenAny(SaveContributionCommandWrapper::class, VoidResult.Accepted) }
    }) exercise {
        cli(exerciseScope, cannon).test("party --party-id=${partyId.value} details")
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.output)
        receivedActions.firstOrNull()
            .assertIsEqualTo(GqlQuery(PartyDetailsQuery(partyId)))
        result.output.trim()
            .assertIsEqualTo("Details retrieved.")
    }
}
