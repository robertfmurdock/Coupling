package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.routing.Commander
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.child

private val LoadedPairAssignments by lazy { couplingDataLoader(SocketedPairAssignments) }

val NewPairAssignmentsPage = tribePageFunction { props, tribeId ->
    child(LoadedPairAssignments, dataLoadProps(tribeId, props.commander), key = tribeId.value)
}

private fun dataLoadProps(tribeId: TribeId, commander: Commander) = dataLoadProps(
    commander = commander,
    query = TribeDataSetQuery(tribeId),
    toProps = { reload, commandFunc, (tribe, players, pairAssignments) ->
        val controls = Controls(commandFunc, reload)
        SocketedPairAssignmentsProps(tribe, players, pairAssignments.firstOrNull(), controls, true)
    }
)
