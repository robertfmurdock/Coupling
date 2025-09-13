package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.contribution.ContributionContentFrame
import com.zegreatrob.coupling.client.components.contribution.ContributionListContent
import com.zegreatrob.coupling.client.components.player.UpdatingPlayerList
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery
import js.lazy.Lazy

@Lazy
val ContributionListPage = partyPageFunction { props, partyId ->
    val (window: GqlContributionWindow, setWindow) = useWindow(GqlContributionWindow.Week)
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                playerList()
                retiredPlayers()
                contributionReport(window = window) { contributions() }
            }
        },
        key = "${partyId.value}$window",
    ) { reload, dispatchFunc, queryResult ->
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
                ContributionListContent(
                    party,
                    contributions,
                    window,
                    setWindow,
                    playerList,
                    dispatchFunc,
                )
            }
        }
    }
}
