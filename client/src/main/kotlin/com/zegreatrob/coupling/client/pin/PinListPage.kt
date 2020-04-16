package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.routing.DataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import react.RBuilder

private val LoadedPinList = dataLoadWrapper(PinList)
private val RBuilder.loadedPinList get() = LoadedPinList.render(this)

val PinListPage = tribePageFunction { props, tribeId ->
    loadedPinList(DataLoadProps { _, _ ->
        props.commander.runQuery { TribePinListQuery(tribeId).perform() }
            .toPinListProps(props.pathSetter)
    })
}

private fun Pair<Tribe?, List<Pin>>.toPinListProps(pathSetter: (String) -> Unit) = let { (tribe, pins) ->
    PinListProps(tribe!!, pins, pathSetter)
}
