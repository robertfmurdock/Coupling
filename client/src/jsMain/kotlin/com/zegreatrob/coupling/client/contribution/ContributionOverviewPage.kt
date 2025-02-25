package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.contribution.ContributionContentFrame
import com.zegreatrob.coupling.client.components.contribution.ContributionOverviewContent
import com.zegreatrob.coupling.client.components.contribution.ContributionStartContent
import com.zegreatrob.coupling.client.components.player.UpdatingPlayerList
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery

val ContributionOverviewPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                playerList()
                retiredPlayers()
                contributionReport(limit = 5) { contributions() }
            }
        },
        key = partyId.value,
    ) { _, dispatchFunc, queryResult ->
        val party = queryResult.party?.details?.data ?: return@CouplingQuery
        val contributions =
            queryResult.party?.contributionReport?.contributions?.elements ?: return@CouplingQuery
        val players = queryResult.party?.playerList?.elements ?: return@CouplingQuery
        val retiredPlayers = queryResult.party?.retiredPlayers?.elements ?: return@CouplingQuery
        UpdatingPlayerList(
            players = players + retiredPlayers,
            dispatchFunc = dispatchFunc,
        ) { playerList, dispatchFunc ->
            ContributionContentFrame(party = party) {
                if (contributions.isEmpty()) {
                    ContributionStartContent(party)
                } else {
                    ContributionOverviewContent(party, contributions, playerList, dispatchFunc)
                }
            }
        }
    }
}
