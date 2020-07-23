package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.couplingDataLoadWrapper
import com.zegreatrob.coupling.client.tribePageFunction

private val LoadedPin by lazy { couplingDataLoadWrapper(PinConfig) }

val PinPage = tribePageFunction { props, tribeId ->
    val pinId = props.pinId
    child(LoadedPin, dataLoadProps(
        commander = props.commander,
        query = TribePinQuery(tribeId, pinId),
        toProps = { reload, commandFunc, (tribe, pins, pin) ->
            PinConfigProps(tribe, pin, pins, props.pathSetter, reload, commandFunc)
        }
    )) { pinId?.let { attrs { key = it } } }
}
