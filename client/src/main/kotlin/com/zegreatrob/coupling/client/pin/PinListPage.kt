package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.minreact.create
import react.key

private val LoadedPinList = couplingDataLoader<PinList>()

val PinListPage = partyPageFunction { props, partyId ->
    +dataLoadProps(
        LoadedPinList,
        commander = props.commander,
        query = PartyPinListQuery(partyId),
        toProps = { _, _, (party, pins) -> PinList(party, pins) }
    ).create {
        key = partyId.value
    }
}
