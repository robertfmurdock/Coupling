package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.routing.Commander
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.child

private val LoadedPairAssignments by lazy { couplingDataLoader(SocketedPairAssignments) }

val CurrentPairsPage = tribePageFunction { props, tribeId ->
    child(LoadedPairAssignments, dataLoadProps(tribeId, props.pathSetter, props.commander), key = tribeId.value)
}

private fun dataLoadProps(tribeId: TribeId, pathSetter: (String) -> Unit, commander: Commander) = dataLoadProps(
    commander = commander,
    query = TribeDataSetQuery(tribeId),
    toProps = { reload, dispatchFunc, (tribe, players, history) ->
        val controls = Controls(dispatchFunc, pathSetter, reload)
        SocketedPairAssignmentsProps(tribe, players, history.firstOrNull(), controls, false)
    }
)
