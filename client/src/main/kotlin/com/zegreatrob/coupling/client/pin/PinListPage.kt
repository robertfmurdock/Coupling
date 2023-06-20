package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.create

val PinListPage = partyPageFunction { props, partyId ->
    +CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                party()
                pinList()
            }
        },
        toDataprops = { _, _, result ->
            PinList(
                party = result.party?.details?.data ?: return@CouplingQuery null,
                pins = result.party?.pinList?.elements ?: return@CouplingQuery null,
            )
        },
    ).create(key = partyId.value)
}
