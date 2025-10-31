package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.contribution.ContributionContentFrame
import com.zegreatrob.coupling.client.components.contribution.ContributionListContent
import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import com.zegreatrob.coupling.client.components.player.UpdatingPlayerList
import com.zegreatrob.coupling.client.gql.ContributionListPageQuery
import com.zegreatrob.coupling.client.party.toModel
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.toModel
import js.lazy.Lazy

@Lazy
val ContributionListPage = partyPageFunction { props, partyId ->
    val (window, setWindow) = useWindow(ContributionWindow.Week)
    CouplingQuery(
        commander = props.commander,
        query = ApolloGraphQuery(ContributionListPageQuery(partyId, window.toGql())),
        key = "${partyId.value}$window",
    ) { _, dispatchFunc, queryResult ->
        val party = queryResult.party?.details?.partyDetailsFragment?.toModel() ?: return@CouplingQuery
        val contributions =
            queryResult.party.contributionReport?.contributions?.map { it.contributionFragment.toModel() }
                ?: return@CouplingQuery
        val players = queryResult.party.playerList?.map { it.playerDetailsFragment.toModel() } ?: return@CouplingQuery
        val retiredPlayers =
            queryResult.party.retiredPlayers?.map { it.playerDetailsFragment.toModel() } ?: return@CouplingQuery
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
