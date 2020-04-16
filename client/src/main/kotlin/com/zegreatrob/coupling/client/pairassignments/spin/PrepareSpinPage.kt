package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.pairassignments.TribeDataSetQuery
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.tribePageFunction
import react.RBuilder

private val LoadedPairAssignments by lazy { dataLoadWrapper(PrepareSpin) }
private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.render(this)

val PrepareSpinPage = tribePageFunction { props, tribeId ->
    loadedPairAssignments(dataLoadProps(
        commander = props.commander,
        query = { TribeDataSetQuery(tribeId).perform() },
        toProps = { _, _, (tribe, players, history, pins) ->
            PrepareSpinProps(tribe, players, history, pins, props.pathSetter)
        }
    ))
}
