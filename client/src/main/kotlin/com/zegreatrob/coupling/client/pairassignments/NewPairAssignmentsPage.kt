package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.builder
import com.zegreatrob.coupling.client.routing.Commander
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder

private val LoadedPairAssignments by lazy { dataLoadWrapper(PairAssignments) }

private val RBuilder.loadedPairAssignments get() = builder(LoadedPairAssignments)

val NewPairAssignmentsPage = tribePageFunction { props, tribeId ->
    with(props) {
        val playerIds = search.getAll("player").toList()
        val pinIds = search.getAll("pin").toList()
        loadedPairAssignments(dataLoadProps(tribeId, playerIds, pinIds, pathSetter, commander))
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
        PairAssignmentsProps(tribe!!, players, pairAssignments, commandFunc, pathSetter)
    }
)
