package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.pairassignments.TribeCurrentDataQuery
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.minreact.child

private val LoadedPairAssignments by lazy { couplingDataLoader(PrepareSpin) }

val PrepareSpinPage = tribePageFunction { props, tribeId ->
    child(LoadedPairAssignments, dataLoadProps(
        commander = props.commander,
        query = TribeCurrentDataQuery(tribeId),
        toProps = { _, dispatcher, (tribe, players, currentPairsDoc, pins) ->
            PrepareSpinProps(tribe, players, currentPairsDoc, pins, dispatcher)
        }
    ), key = tribeId.value)
}
