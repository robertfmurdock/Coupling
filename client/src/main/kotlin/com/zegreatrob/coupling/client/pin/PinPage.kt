package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.minreact.child

private val LoadedPin by lazy { couplingDataLoader(PinConfig) }

val PinPage = tribePageFunction { props, tribeId ->
    val pinId = props.pinId
    child(LoadedPin, dataLoadProps(
        commander = props.commander,
        query = TribePinQuery(tribeId, pinId),
        toProps = { reload, commandFunc, (tribe, pins, pin) ->
            PinConfigProps(tribe, pin, pins, reload, commandFunc)
        }
    ), key = pinId)
}
