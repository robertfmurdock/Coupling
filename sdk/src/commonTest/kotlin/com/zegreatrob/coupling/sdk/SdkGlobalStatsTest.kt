package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import korlibs.time.DateTime
import kotlin.test.Test

class SdkGlobalStatsTest {

    @Test
    fun canGetGlobalStats() = asyncSetup(object {
        val now = DateTime.now().year
        val party = stubParty()
    }) {
        sdk().perform(SavePartyCommand(party))
        sdk().perform(SavePairAssignmentsCommand(party.id, stubPairAssignmentDoc().copy(date = DateTime.now())))
    } exercise {
        sdk().perform(
            graphQuery {
                globalStats(now)
            },
        )
    } verify { result ->
        result?.globalStats?.parties?.size
            .assertIsNotEqualTo(0)
    }
}
