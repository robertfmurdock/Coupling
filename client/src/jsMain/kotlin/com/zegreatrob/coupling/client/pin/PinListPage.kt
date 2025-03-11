package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery

val PinListPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                pinList()
            }
        },
        key = partyId.value.toString(),
    ) { _, _, result ->
        PinList(
            party = result.party?.details?.data ?: return@CouplingQuery,
            pins = result.party?.pinList?.elements ?: return@CouplingQuery,
        )
    }
}
