package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.routing.Commander
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.child

private val LoadedPairAssignments by lazy { couplingDataLoader(SocketedPairAssignments) }

val NewPairAssignmentsPage = tribePageFunction { props, tribeId ->
    with(props) {
        val playerIds = search.getAll("player").toList()
        val pinIds = search.getAll("pin").toList()
        child(
            LoadedPairAssignments,
            dataLoadProps(tribeId, playerIds, pinIds, pathSetter, commander),
            key = tribeId.value
        )
    }
}

private fun dataLoadProps(
    tribeId: TribeId,
    playerIds: List<String>,
    pinIds: List<String>,
    pathSetter: (String) -> Unit,
    commander: Commander
) = dataLoadProps(
    commander = commander,
    query = NewPairAssignmentsQuery(tribeId, playerIds, pinIds),
    toProps = { _, commandFunc, (tribe, players, pairAssignments) ->
        SocketedPairAssignmentsProps(tribe, players, pairAssignments, commandFunc, pathSetter)
    }
)
