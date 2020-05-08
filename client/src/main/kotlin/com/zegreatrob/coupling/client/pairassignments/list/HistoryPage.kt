package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.tribePageFunction
import react.RBuilder

private val LoadedPairAssignments by lazy{ dataLoadWrapper(History)}
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.render(this)

val HistoryPage = tribePageFunction { props, tribeId ->
    loadedPairAssignments(dataLoadProps(
        commander = props.commander,
        query = { HistoryQuery(tribeId).perform() },
        toProps = { reload, commandFunc, (tribe, history) ->
            HistoryProps(tribe!!, history, reload, props.pathSetter, DispatchFunc(commandFunc))
        }
    ))
}
