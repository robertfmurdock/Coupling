package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.minreact.child

private val LoadedPinList = couplingDataLoader(PinList)

val PinListPage = tribePageFunction { props, tribeId ->
    child(LoadedPinList, dataLoadProps(
        commander = props.commander,
        query = TribePinListQuery(tribeId),
        toProps = { _, _, (tribe, pins) -> PinListProps(tribe, pins) }
    ), key = tribeId.value)
}
