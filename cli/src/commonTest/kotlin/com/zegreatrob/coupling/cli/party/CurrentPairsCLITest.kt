package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.test
import com.zegreatrob.coupling.cli.cli
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSetId
import com.zegreatrob.coupling.model.pairassignmentdocument.callSign
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.CurrentPairAssignmentsQuery
import com.zegreatrob.coupling.sdk.schema.type.PairingSetMap
import com.zegreatrob.coupling.sdk.schema.type.PartyBuilder
import com.zegreatrob.coupling.sdk.schema.type.buildPairSnapshot
import com.zegreatrob.coupling.sdk.schema.type.buildPairingSet
import com.zegreatrob.coupling.sdk.schema.type.buildParty
import com.zegreatrob.coupling.sdk.schema.type.buildPlayerSnapshot
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.testaction.StubCannon
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test
import kotlin.time.Clock

class CurrentPairsCLITest {

    @Test
    fun willReturnCurrentPairsFromQuery() = asyncSetup(object : ScopeMint() {
        val partyId = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val expected = GqlQuery(CurrentPairAssignmentsQuery(partyId))

        val expectedPair = pairOf(stubPlayer(), stubPlayer())

        val result = CurrentPairAssignmentsQuery.Data {
            this.party = buildParty {
                this.id = partyId
                this.currentPairingSet = buildCurrentPairingSet(this@buildParty, expectedPair, partyId)
            }
        }
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also { it.givenAny(expected::class, result) }
    }) exercise {
        cli(exerciseScope, cannon).test("party --party-id=${partyId.value} current-pairs")
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.output)
        receivedActions.firstOrNull()
            .assertIsEqualTo(expected)
        result.output.trim()
            .assertIsEqualTo(
                """
Current Pairs for Party ID: ${partyId.value}

- ${expectedPair.withPins(emptySet()).callSign()}
  ${expectedPair.player1.name} & ${expectedPair.player2.name}

                    """
                    .trim(),
            )
    }

    private fun buildCurrentPairingSet(
        builder: PartyBuilder,
        expectedPair: CouplingPair.Double,
        thePartyId: PartyId,
    ): PairingSetMap = builder.buildPairingSet {
        this.partyId = thePartyId
        this.id = PairingSetId.new()
        this.date = Clock.System.now()
        this.pairs = listOf(
            buildPairSnapshot {
                this.pins = emptyList()
                this.players = expectedPair.map {
                    buildPlayerSnapshot {
                        this.id = it.id
                        this.name = it.name
                        this.callSignNoun = it.callSignNoun
                        this.callSignAdjective = it.callSignAdjective
                        this.pins = emptyList()
                    }
                }
            },
        )
    }

    @Test
    fun whenPartyDoesNotExistReturnFailure() = asyncSetup(object : ScopeMint() {
        val partyId = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val expected = GqlQuery(CurrentPairAssignmentsQuery(partyId))
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also { it.given(expected, CurrentPairAssignmentsQuery.Data { party = null }) }
    }) exercise {
        cli(exerciseScope, cannon).test("party --party-id=${partyId.value} current-pairs")
    } verify { result ->
        result.statusCode.assertIsEqualTo(1, result.output)
        result.output.trim()
            .assertIsEqualTo("Party not found.")
    }
}
