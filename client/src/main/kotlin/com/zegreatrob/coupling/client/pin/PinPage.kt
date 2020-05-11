package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.tribePageFunction
import react.RBuilder

private val LoadedPin by lazy { dataLoadWrapper(PinConfig) }
private val RBuilder.loadedPin get() = LoadedPin.render(this)

val PinPage = tribePageFunction { props, tribeId ->
    val pinId = props.pinId
    loadedPin(dataLoadProps(
        commander = props.commander,
        query = { TribePinQuery(tribeId, pinId).perform() },
        toProps = { reload, commandFunc, (tribe, pins, pin) ->
            PinConfigProps(tribe!!, pin, pins, props.pathSetter, reload, commandFunc)
        }
    )) { pinId?.let { attrs { key = it } } }
}
