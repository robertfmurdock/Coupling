package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.pairassignments.TribeDataSetQuery
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.minreact.child

private val LoadedPairAssignments by lazy { couplingDataLoader(PrepareSpin) }

val PrepareSpinPage = tribePageFunction { props, tribeId ->
    child(LoadedPairAssignments, dataLoadProps(
        commander = props.commander,
        query = TribeDataSetQuery(tribeId),
        toProps = { _, _, (tribe, players, history, pins) ->
            PrepareSpinProps(tribe, players, history, pins, props.pathSetter)
        }
    ), key = tribeId.value)
}
