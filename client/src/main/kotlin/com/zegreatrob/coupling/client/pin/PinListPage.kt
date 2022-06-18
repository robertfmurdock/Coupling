package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.minreact.add
import react.key

private val LoadedPinList = couplingDataLoader<PinList>()

val PinListPage = partyPageFunction { props, partyId ->
    add(
        dataLoadProps(
            LoadedPinList,
            commander = props.commander,
            query = PartyPinListQuery(partyId),
            toProps = { _, _, (party, pins) -> PinList(party, pins) }
        )
    ) {
        key = partyId.value
    }
}
