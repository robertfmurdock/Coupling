package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.contribution.ContributionContentFrame
import com.zegreatrob.coupling.client.components.contribution.ContributionListContent
import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import com.zegreatrob.coupling.client.components.player.UpdatingPlayerList
import com.zegreatrob.coupling.client.gql.ContributionListPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.adapter.toModel
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.toModel
import js.lazy.Lazy

@Lazy
val ContributionListPage = partyPageFunction { props, partyId ->
    val (window, setWindow) = useWindow(ContributionWindow.Week)
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(ContributionListPageQuery(partyId, window.toGql())),
        key = "${partyId.value}$window",
    ) { _, dispatchFunc, queryResult ->
        val party = queryResult.party?.partyDetails?.toModel() ?: return@CouplingQuery
        val contributions =
            queryResult.party.contributionReport?.contributions?.map { it.contributionDetails.toModel() }
                ?: return@CouplingQuery
        val players = queryResult.party.playerList?.map { it.playerDetails.toModel() } ?: return@CouplingQuery
        val retiredPlayers =
            queryResult.party.retiredPlayers?.map { it.playerDetails.toModel() } ?: return@CouplingQuery
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
