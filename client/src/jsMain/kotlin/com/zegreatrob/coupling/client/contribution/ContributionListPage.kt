package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.contribution.ContributionContentFrame
import com.zegreatrob.coupling.client.components.contribution.ContributionListContent
import com.zegreatrob.coupling.client.components.contribution.create
import com.zegreatrob.coupling.client.components.player.UpdatingPlayerList
import com.zegreatrob.coupling.client.components.player.create
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery

val ContributionListPage = partyPageFunction { props, partyId ->
    val (window: GqlContributionWindow, setWindow) = useWindow(GqlContributionWindow.Week)
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                playerList()
                contributionReport(window = window) { contributions() }
            }
        },
        toNode = { reload, dispatchFunc, queryResult ->
            val party = queryResult.party?.details?.data ?: return@CouplingQuery null
            val contributions =
                queryResult.party?.contributionReport?.contributions?.elements ?: return@CouplingQuery null

            UpdatingPlayerList.create(
                players = queryResult.party?.playerList?.elements ?: return@CouplingQuery null,
                dispatchFunc = dispatchFunc,
                child = { playerList, dispatchFunc ->
                    ContributionContentFrame.create(party = party) {
                        ContributionListContent(
                            party,
                            contributions,
                            window,
                            setWindow,
                            playerList,
                            dispatchFunc,
                        )
                    }
                },
            )
        },
        key = "${partyId.value}$window",
    )
}
