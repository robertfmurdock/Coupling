package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.test
import com.zegreatrob.coupling.cli.cli
import com.zegreatrob.coupling.cli.gql.PlayersQuery
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.type.PartyInput
import com.zegreatrob.coupling.sdk.schema.type.buildParty
import com.zegreatrob.coupling.sdk.schema.type.buildPlayer
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.testaction.StubCannon
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class PlayersCLITest {

    @Test
    fun willReturnPlayersFromQuery() = asyncSetup(object : ScopeMint() {
        val partyId = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val players = listOf(stubPlayer(), stubPlayer(), stubPlayer())
        val expected = GqlQuery(PlayersQuery(PartyInput(partyId)))
        val detailsSlice = PlayersQuery.Data {
            this.party = buildParty {
                this.playerList = players.map {
                    buildPlayer {
                        this.id = it.id
                        this.name = it.name
                        this.email = it.email
                    }
                }
            }
        }
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also { it.givenAny(expected::class, detailsSlice) }
    }) exercise {
        cli(cannon).test("party --party-id=${partyId.value} players")
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.output)
        receivedActions.firstOrNull()
            .assertIsEqualTo(expected)
        result.output.trim()
            .assertIsEqualTo(
                "Players for Party ID: ${partyId.value}\n${players.joinToString("\n") { "  - ${it.name} ${it.email}" }}",
            )
    }
}
