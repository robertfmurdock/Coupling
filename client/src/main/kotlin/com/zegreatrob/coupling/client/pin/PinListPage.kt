package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.minreact.create

val PinListPage = partyPageFunction { props, partyId ->
    +CouplingQuery(
        commander = props.commander,
        query = PartyPinListQuery(partyId),
        toDataprops = { _, _, (party, pins) -> PinList(party, pins) }
    ).create(key = partyId.value)
}
