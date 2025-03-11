package com.zegreatrob.coupling.client.integration

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.graphQuery

val IntegrationPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                integration()
            }
            config { addToSlackUrl() }
        },
        key = partyId.value.toString(),
    ) { _, _, result ->
        IntegrationContent(
            party = result.party?.details?.data ?: return@CouplingQuery,
            integration = result.party?.integration?.data,
            addToSlackUrl = result.config?.addToSlackUrl ?: return@CouplingQuery,
        )
    }
}
