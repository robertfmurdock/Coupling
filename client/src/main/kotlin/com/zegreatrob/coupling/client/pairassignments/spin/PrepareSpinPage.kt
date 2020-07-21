package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.minreact.child
import com.zegreatrob.coupling.client.pairassignments.TribeDataSetQuery
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.tribePageFunction

private val LoadedPairAssignments by lazy { dataLoadWrapper(PrepareSpin) }

val PrepareSpinPage = tribePageFunction { props, tribeId ->
    child(LoadedPairAssignments, dataLoadProps(
        commander = props.commander,
        query = TribeDataSetQuery(tribeId),
        toProps = { _, _, (tribe, players, history, pins) ->
            PrepareSpinProps(tribe, players, history, pins, props.pathSetter)
        }
    ))
}
