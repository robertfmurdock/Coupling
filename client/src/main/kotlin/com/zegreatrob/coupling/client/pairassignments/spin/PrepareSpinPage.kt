package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.external.react.builder
import com.zegreatrob.coupling.client.pairassignments.TribeDataSetQuery
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.tribePageFunction
import react.RBuilder

private val LoadedPairAssignments by lazy { dataLoadWrapper(PrepareSpin) }
private val RBuilder.loadedPairAssignments get() = builder(LoadedPairAssignments)

val PrepareSpinPage = tribePageFunction { props, tribeId ->
    loadedPairAssignments(dataLoadProps(
        commander = props.commander,
        query = TribeDataSetQuery(tribeId),
        toProps = { _, _, (tribe, players, history, pins) ->
            PrepareSpinProps(tribe, players, history, pins, props.pathSetter)
        }
    ))
}
