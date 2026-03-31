package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.test
import com.zegreatrob.coupling.cli.cli
import com.zegreatrob.coupling.cli.gql.PartyListQuery
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.type.buildParty
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.testaction.StubCannon
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class PartyListCLITest {

    @Test
    fun willReturnFormattedPartyList() = asyncSetup(object : ScopeMint() {
        val partyId1 = stubPartyId()
        val partyId2 = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val expected = GqlQuery(PartyListQuery())
        val result = PartyListQuery.Data {
            this.partyList = listOf(
                buildParty {
                    this.id = partyId1
                    this.name = "Alpha"
                },
                buildParty {
                    this.id = partyId2
                    this.name = "Beta"
                },
            )
        }
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also { it.givenAny(expected::class, result) }
    }) exercise {
        cli(cannon).test("party list")
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.output)
        receivedActions.firstOrNull()
            .assertIsEqualTo(expected)
        result.output.trim()
            .assertIsEqualTo(
                """
                Party: id = ${partyId1.value}, name = Alpha
                Party: id = ${partyId2.value}, name = Beta
                """
                    .trimIndent(),
            )
    }
}
