package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.builder
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import react.RBuilder

private val LoadedPinList = dataLoadWrapper(PinList)
private val RBuilder.loadedPinList get() = builder(LoadedPinList)

val PinListPage = tribePageFunction { props, tribeId ->
    loadedPinList(dataLoadProps(
        commander = props.commander,
        query = TribePinListQuery(tribeId),
        toProps = { _, _, it -> it.toPinListProps(props.pathSetter) }
    ))
}

private fun Pair<Tribe?, List<Pin>>.toPinListProps(pathSetter: (String) -> Unit) = let { (tribe, pins) ->
    PinListProps(tribe!!, pins, pathSetter)
}
