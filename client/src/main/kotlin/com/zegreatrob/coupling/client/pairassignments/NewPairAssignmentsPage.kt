package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.routing.Commander
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder

private val LoadedPairAssignments by lazy { dataLoadWrapper(PairAssignments) }

private val RBuilder.loadedPairAssignments get() = LoadedPairAssignments.render(this)

val NewPairAssignmentsPage = reactFunction<PageProps> { props ->
    val tribeId = props.tribeId
    if (tribeId != null) {
        with(props) {
            val playerIds = search.getAll("player").toList()
            val pinIds = search.getAll("pin").toList()
            loadedPairAssignments(dataLoadProps(tribeId, playerIds, pinIds, pathSetter, commander))
        }
    } else throw Exception("WHAT")
}

private fun dataLoadProps(
    tribeId: TribeId,
    playerIds: List<String>,
    pinIds: List<String>,
    pathSetter: (String) -> Unit,
    commander: Commander
) = dataLoadProps(
    commander = commander,
    query = { NewPairAssignmentsQuery(tribeId, playerIds, pinIds).perform() },
    toProps = { _, commandFunc, (tribe, players, pairAssignments) ->
        PairAssignmentsProps(tribe!!, players, pairAssignments, commandFunc, pathSetter)
    }
)
