package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.contribution.ContributionContentFrame
import com.zegreatrob.coupling.client.components.contribution.ContributionListContent
import com.zegreatrob.coupling.client.components.contribution.create
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.Contributor
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery

val ContributionListPage = partyPageFunction { props, partyId ->
    val (window: GqlContributionWindow, setWindow) = useWindow(GqlContributionWindow.Quarter)
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                contributionReport(window = window) {
                    contributions()
                    contributors { details() }
                }
            }
        },
        toNode = { reload, _, queryResult ->
            val party = queryResult.party?.details?.data ?: return@CouplingQuery null
            val contributions =
                queryResult.party?.contributionReport?.contributions?.elements ?: return@CouplingQuery null
            val contributors = queryResult.party
                ?.contributionReport
                ?.contributors
                ?.mapNotNull(Contributor::details)
                ?.elements
                ?: return@CouplingQuery null
            ContributionContentFrame.create(party = party) {
                ContributionListContent(party, contributions, contributors, window, {
                    setWindow(it)
                    reload()
                })
            }
        },
        key = partyId.value,
    )
}
