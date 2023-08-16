package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.action.stats.StatisticsQuery
import com.zegreatrob.coupling.action.stats.StatisticsReport
import com.zegreatrob.coupling.action.stats.heatmap.heatmapData
import com.zegreatrob.coupling.client.components.stats.PartyStatistics
import com.zegreatrob.coupling.client.components.stats.create
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
                pairAssignmentDocumentList()
                pairs {
                    players()
                    spinsSinceLastPaired()
                    heat()
                }
                medianSpinDuration()
                spinsUntilFullRotation()
            }
        },
        toNode = { _, _, queryResult ->
            val party = queryResult.party ?: return@CouplingQuery null
            val players = party.playerList?.elements ?: return@CouplingQuery null
            val pairs = party.pairs ?: return@CouplingQuery null
            PartyStatistics.create(
                StatisticsQuery.Results(
                    party = party.details?.data ?: return@CouplingQuery null,
                    players = players,
                    history = party.pairAssignmentDocumentList?.elements ?: return@CouplingQuery null,
                    pairs = pairs,
                    report = StatisticsReport(
                        spinsUntilFullRotation = party.spinsUntilFullRotation ?: return@CouplingQuery null,
                        medianSpinDuration = party.medianSpinDuration,
                    ),
                    heatmapData = heatmapData(players, pairs),
                ),
            )
        },
        key = partyId.value,
    )
}
