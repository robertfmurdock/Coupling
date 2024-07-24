package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.contribution.ContributionContentFrame
import com.zegreatrob.coupling.client.components.contribution.ContributionOverviewContent
import com.zegreatrob.coupling.client.components.contribution.ContributionStartContent
import com.zegreatrob.coupling.client.components.contribution.create
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.Contributor
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery

val ContributionOverviewPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                contributionReport(limit = 5) {
                    contributions()
                    contributors { details() }
                }
            }
        },
        toNode = { _, _, queryResult ->
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
                if (contributions.isEmpty()) {
                    ContributionStartContent(party)
                } else {
                    ContributionOverviewContent(party, contributions, contributors)
                }
            }
        },
        key = partyId.value,
    )
}
