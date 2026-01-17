package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.test
import com.zegreatrob.coupling.cli.cli
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import com.zegreatrob.coupling.sdk.schema.PartyDetailsQuery
import com.zegreatrob.coupling.sdk.schema.type.buildParty
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
        val expected = GqlQuery(PartyDetailsQuery(partyId))
        val detailsSlice = PartyDetailsQuery.Data {
            this.party = buildParty {
                this.id = partyId
            }
        }
        val expectedDetails = detailsSlice.party?.partyDetails?.toDomain()
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also { it.givenAny(expected::class, detailsSlice) }
    }) exercise {
        cli(exerciseScope, cannon).test("party --party-id=${partyId.value} details")
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.output)
        receivedActions.firstOrNull()
            .assertIsEqualTo(expected)
        result.output.trim()
            .assertIsEqualTo(
                """
                    Party ID: $partyId
                    Name: ${expectedDetails?.name}
                    Email: ${expectedDetails?.email}
                    PairingRule: ${expectedDetails?.pairingRule}
                    BadgesEnabled: ${expectedDetails?.badgesEnabled}
                    DefaultBadgeName: ${expectedDetails?.defaultBadgeName}
                    AlternateBadgeName: ${expectedDetails?.alternateBadgeName}
                    CallSignsEnabled: ${expectedDetails?.callSignsEnabled}
                    AnimationEnabled: ${expectedDetails?.animationEnabled}
                    AnimationSpeed: ${expectedDetails?.animationSpeed}"""
                    .trimIndent(),
            )
    }

    @Test
    fun whenPartyDoesNotExistReturnFailure() = asyncSetup(object : ScopeMint() {
        val partyId = stubPartyId()
        val receivedActions = mutableListOf<Any?>()
        val expected = GqlQuery(PartyDetailsQuery(partyId))
        val cannon = StubCannon<CouplingSdkDispatcher>(receivedActions)
            .also { it.given(expected, PartyDetailsQuery.Data { party = null }) }
    }) exercise {
        cli(exerciseScope, cannon).test("party --party-id=${partyId.value} details")
    } verify { result ->
        result.statusCode.assertIsEqualTo(1, result.output)
        result.output.trim()
            .assertIsEqualTo("Party not found.")
    }
}
