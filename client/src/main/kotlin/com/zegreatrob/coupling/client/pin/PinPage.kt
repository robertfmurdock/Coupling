package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder

private val LoadedPin by lazy { dataLoadWrapper(PinConfig) }
private val RBuilder.loadedPin get() = LoadedPin.render(this)

val PinPage = reactFunction<PageProps> { props ->
    val tribeId = props.tribeId
    val pinId = props.pinId

    if (tribeId != null) {
        loadedPin(dataLoadProps(
            commander = props.commander,
            query = { TribePinQuery(tribeId, pinId).perform() },
            toProps = { reload, commandFunc, (tribe, pins, pin) ->
                PinConfigProps(tribe!!, pin, pins, props.pathSetter, reload, commandFunc)
            }
        )) { pinId?.let { attrs { key = it } } }
    } else throw Exception("WHAT")
}
