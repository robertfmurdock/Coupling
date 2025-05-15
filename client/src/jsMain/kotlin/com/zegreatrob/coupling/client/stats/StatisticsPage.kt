package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.components.graphing.CouplingResponsiveLine
import com.zegreatrob.coupling.client.components.stats.PartyStatistics
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery

val StatisticsPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                playerList()
                pairs(includeRetired = false) {
                    players()
                    spinsSinceLastPaired()
                    recentTimesPaired()
                    pairAssignmentHistory {
                        date()
                        recentTimesPaired()
                    }
                }
                medianSpinDuration()
                spinsUntilFullRotation()
            }
        },
        key = partyId.value.toString(),
    ) { _, _, queryResult ->
        PartyStatistics(
            party = queryResult.party?.details?.data ?: return@CouplingQuery,
            players = queryResult.party?.playerList?.elements ?: return@CouplingQuery,
            pairs = queryResult.party?.pairs ?: return@CouplingQuery,
            spinsUntilFullRotation = queryResult.party?.spinsUntilFullRotation ?: return@CouplingQuery,
            medianSpinDuration = queryResult.party?.medianSpinDuration,
            chartComponent = CouplingResponsiveLine,
        )
    }
}
