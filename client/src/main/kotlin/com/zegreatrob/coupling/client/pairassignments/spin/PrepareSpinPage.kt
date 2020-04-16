package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.pairassignments.TribeDataSetQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder

private val LoadedPairAssignments by lazy { dataLoadWrapper(PrepareSpin) }
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.render(this)

val PrepareSpinPage = reactFunction<PageProps> { props ->
    val tribeId = props.tribeId
    if (tribeId != null) {
        loadedPairAssignments(dataLoadProps(
            commander = props.commander,
            query = { TribeDataSetQuery(tribeId).perform() },
            toProps = { _, _, (tribe, players, history, pins) ->
                PrepareSpinProps(tribe, players, history, pins, props.pathSetter)
            }
        ))
    } else throw Exception("WHAT")
}
