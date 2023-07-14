package com.zegreatrob.coupling.client.integration

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.graphQuery

val IntegrationPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                party()
                integration()
            }
            addToSlackUrl()
        },
        toNode = { _, _, result ->
            IntegrationContent.create(
                party = result.party?.details?.data ?: return@CouplingQuery null,
                integration = result.party?.integration?.data,
                addToSlackUrl = result.addToSlackUrl ?: return@CouplingQuery null,
            )
        },
        key = partyId.value,
    )
}
