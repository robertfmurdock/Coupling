package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder

private val LoadedPairAssignments = dataLoadWrapper(History)
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.render(this)

val HistoryPage = reactFunction<PageProps> { props ->
    val tribeId = props.tribeId
    if (tribeId != null) {
        loadedPairAssignments(dataLoadProps(
            commander = props.commander,
            query = { HistoryQuery(tribeId).perform() },
            toProps = { reload, commandFunc, (tribe, history) ->
                HistoryProps(tribe!!, history, reload, props.pathSetter, commandFunc)
            }
        ))
    } else throw Exception("WHAT")
}
