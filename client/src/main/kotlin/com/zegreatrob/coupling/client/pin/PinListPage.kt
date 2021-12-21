package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.minreact.child

private val LoadedPinList = couplingDataLoader<PinList>()

val PinListPage = tribePageFunction { props, tribeId ->
    child(dataLoadProps(
        LoadedPinList,
        commander = props.commander,
        query = TribePinListQuery(tribeId),
        toProps = { _, _, (tribe, pins) -> PinList(tribe, pins) }
    ), key = tribeId.value)
}
