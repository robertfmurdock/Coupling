package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.schema.GlobalStatsQuery
import com.zegreatrob.coupling.sdk.schema.type.GlobalStatsInput
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.minassert.assertIsNotEqualTo
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class SdkGlobalStatsTest {

    @Test
    fun canGetGlobalStats() = asyncSetup(object {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).year
        val party = stubPartyDetails()
    }) {
        sdk().fire(SavePartyCommand(party))
        sdk().fire(
            SavePairAssignmentsCommand(
                partyId = party.id,
                pairAssignments = stubPairAssignmentDoc().copy(date = Clock.System.now().minus(2.days)),
            ),
        )
        sdk().fire(
            SavePairAssignmentsCommand(
                partyId = party.id,
                pairAssignments = stubPairAssignmentDoc().copy(date = Clock.System.now()),
            ),
        )
    } exercise {
        sdk().fire(ApolloGraphQuery(GlobalStatsQuery(GlobalStatsInput(now))))
    } verify { result ->
        result?.globalStats?.parties?.size
            .assertIsNotEqualTo(0)
    }
}
