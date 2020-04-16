package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.child
import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.routing.Commander
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder

private val LoadedPairAssignments by lazy { dataLoadWrapper(PairAssignments) }

val CurrentPairsPage = reactFunction<PageProps> { props ->
    val tribeId = props.tribeId
    if (tribeId != null) {
        pairAssignments(tribeId, props.pathSetter, props.commander)
    } else throw Exception("WHAT")
}

private fun RBuilder.pairAssignments(tribeId: TribeId, pathSetter: (String) -> Unit, commander: Commander) =
    child(LoadedPairAssignments, dataLoadProps(tribeId, pathSetter, commander))

private fun dataLoadProps(tribeId: TribeId, pathSetter: (String) -> Unit, commander: Commander) = dataLoadProps(
    query = commander.suspendFunc { TribeDataSetQuery(tribeId).perform() },
    toProps = { _, scope, (tribe, players, history) ->
        PairAssignmentsProps(
            tribe,
            players,
            history.firstOrNull(),
            commander.buildCommandFunc(scope),
            pathSetter
        )
    }
)
