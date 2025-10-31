package com.zegreatrob.coupling.client.contribution

import com.apollographql.apollo.api.Optional
import com.zegreatrob.coupling.client.components.contribution.ContributionContentFrame
import com.zegreatrob.coupling.client.components.contribution.ContributionOverviewContent
import com.zegreatrob.coupling.client.components.contribution.ContributionStartContent
import com.zegreatrob.coupling.client.components.player.UpdatingPlayerList
import com.zegreatrob.coupling.client.gql.ContributionOverviewPageQuery
import com.zegreatrob.coupling.client.party.toModel
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.schema.type.ContributionsInput
import com.zegreatrob.coupling.sdk.schema.type.PartyInput
import com.zegreatrob.coupling.sdk.toModel
import js.lazy.Lazy

@Lazy
val ContributionOverviewPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = ApolloGraphQuery(
            ContributionOverviewPageQuery(
                partyInput = PartyInput(partyId),
                contributionReportInput = ContributionsInput(limit = Optional.present(5)),
            ),
        ),
        key = partyId.value.toString(),
    ) { _, dispatchFunc, queryResult ->
        val party = queryResult.party?.details?.partyDetailsFragment?.toModel()
            ?: return@CouplingQuery
        val contributions = queryResult.party.contributionReport?.contributions?.map {
            it.contributionFragment.toModel()
        } ?: return@CouplingQuery
        val players = queryResult.party.playerList?.map { it.playerDetailsFragment.toModel() }
            ?: return@CouplingQuery
        val retiredPlayers = queryResult.party.retiredPlayers?.map { it.playerDetailsFragment.toModel() }
            ?: return@CouplingQuery
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
