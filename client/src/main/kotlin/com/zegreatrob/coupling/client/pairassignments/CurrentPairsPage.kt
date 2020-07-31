package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.routing.Commander
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.child

private val LoadedPairAssignments by lazy { couplingDataLoader(PairAssignments) }

val CurrentPairsPage = tribePageFunction { props, tribeId ->
    child(LoadedPairAssignments, dataLoadProps(tribeId, props.pathSetter, props.commander))
}

private fun dataLoadProps(tribeId: TribeId, pathSetter: (String) -> Unit, commander: Commander) = dataLoadProps(
    commander = commander,
    query = TribeDataSetQuery(tribeId),
    toProps = { _, commandFunc, (tribe, players, history) ->
        PairAssignmentsProps(tribe, players, history.firstOrNull(), commandFunc, pathSetter)
    }
)
