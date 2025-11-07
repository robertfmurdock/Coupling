package com.zegreatrob.coupling.client.contribution

import com.apollographql.apollo.api.Optional
import com.zegreatrob.coupling.client.components.contribution.ContributionContentFrame
import com.zegreatrob.coupling.client.components.contribution.ContributionOverviewContent
import com.zegreatrob.coupling.client.components.contribution.ContributionStartContent
import com.zegreatrob.coupling.client.components.player.UpdatingPlayerList
import com.zegreatrob.coupling.client.gql.ContributionOverviewPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import com.zegreatrob.coupling.sdk.schema.type.ContributionsInput
import com.zegreatrob.coupling.sdk.schema.type.PartyInput
import js.lazy.Lazy

@Lazy
val ContributionOverviewPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(
            ContributionOverviewPageQuery(
                partyInput = PartyInput(partyId),
                contributionReportInput = ContributionsInput(limit = Optional.present(5)),
            ),
        ),
        key = partyId.value.toString(),
    ) { _, dispatchFunc, queryResult ->
        val party = queryResult.party?.partyDetails?.toDomain()
            ?: return@CouplingQuery
        val contributions = queryResult.party.contributionReport?.contributions?.map {
            it.contributionDetails.toDomain()
        } ?: return@CouplingQuery
        val players = queryResult.party.playerList.map { it.playerDetails.toDomain() }
        val retiredPlayers = queryResult.party.retiredPlayers.map { it.playerDetails.toDomain() }
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
