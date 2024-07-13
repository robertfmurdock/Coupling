package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.json.JsonContributionWindow
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery

val IncubatingStatisticsPage = partyPageFunction { props, partyId ->
    val window: JsonContributionWindow? = props.search["window"]?.let { window ->
        JsonContributionWindow.entries.find { it.name == window }
    }
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                playerList()
                pairs {
                    players()
                    contributions(window = window)
                }
            }
        },
        toNode = { _, _, queryResult ->
            val party = queryResult.party ?: return@CouplingQuery null
            IncubatingPartyStatisticsContent.create(
                party = party.details?.data ?: return@CouplingQuery null,
                players = party.playerList?.elements ?: return@CouplingQuery null,
                pairs = party.pairs ?: return@CouplingQuery null,
            )
        },
        key = partyId.value,
    )
}
