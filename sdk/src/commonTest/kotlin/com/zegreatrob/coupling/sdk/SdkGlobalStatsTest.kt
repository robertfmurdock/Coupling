package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.minassert.assertIsNotEqualTo
import korlibs.time.DateTime
import korlibs.time.days
import kotlin.test.Test

class SdkGlobalStatsTest {

    @Test
    fun canGetGlobalStats() = asyncSetup(object {
        val now = DateTime.now().year
        val party = stubPartyDetails()
    }) {
        sdk().fire(SavePartyCommand(party))
        fire(
            sdk(),
            SavePairAssignmentsCommand(
                partyId = party.id,
                pairAssignments = stubPairAssignmentDoc().copy(date = DateTime.now().minus(2.days)),
            ),
        )
        fire(
            sdk(),
            SavePairAssignmentsCommand(
                partyId = party.id,
                pairAssignments = stubPairAssignmentDoc().copy(date = DateTime.now()),
            ),
        )
    } exercise {
        sdk().fire(
            graphQuery {
                globalStats(now)
            },
        )
    } verify { result ->
        result?.globalStats?.parties?.size
            .assertIsNotEqualTo(0)
    }
}
