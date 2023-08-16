package com.zegreatrob.coupling.client.stats

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
            PartyStatistics.create(
                party = party.details?.data ?: return@CouplingQuery null,
                players = party.playerList?.elements ?: return@CouplingQuery null,
                pairs = party.pairs ?: return@CouplingQuery null,
                spinsUntilFullRotation = party.spinsUntilFullRotation ?: return@CouplingQuery null,
                medianSpinDuration = party.medianSpinDuration,
            )
        },
        key = partyId.value,
    )
}
