package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.minreact.child

private val LoadedPinList = couplingDataLoader<PinList>()

val PinListPage = partyPageFunction { props, partyId ->
    child(
        dataLoadProps(
            LoadedPinList,
            commander = props.commander,
            query = PartyPinListQuery(partyId),
            toProps = { _, _, (party, pins) -> PinList(party, pins) }
        ),
        key = partyId.value
    )
}
