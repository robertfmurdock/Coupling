package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.contribution.ContributionContentFrame
import com.zegreatrob.coupling.client.components.contribution.ContributionOverviewContent
import com.zegreatrob.coupling.client.components.contribution.ContributionStartContent
import com.zegreatrob.coupling.client.components.contribution.create
import com.zegreatrob.coupling.client.components.player.UpdatingPlayerList
import com.zegreatrob.coupling.client.components.player.create
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
                contributionReport(limit = 5) { contributions() }
            }
        },
        toNode = { _, dispatchFunc, queryResult ->
            val party = queryResult.party?.details?.data ?: return@CouplingQuery null
            val contributions =
                queryResult.party?.contributionReport?.contributions?.elements ?: return@CouplingQuery null
            UpdatingPlayerList.create(
                players = queryResult.party?.playerList?.elements ?: return@CouplingQuery null,
                dispatchFunc = dispatchFunc,
                child = { playerList, dispatchFunc ->
                    ContributionContentFrame.create(party = party) {
                        if (contributions.isEmpty()) {
                            ContributionStartContent(party)
                        } else {
                            ContributionOverviewContent(party, contributions, playerList, dispatchFunc)
                        }
                    }
                },
            )
        },
        key = partyId.value,
    )
}
