package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.coupling.client.child

private val LoadedPairAssignments by lazy { couplingDataLoader(History) }

val HistoryPage = tribePageFunction { props, tribeId ->
    child(LoadedPairAssignments, dataLoadProps(
        commander = props.commander,
        query = HistoryQuery(tribeId),
        toProps = { reload, commandFunc, (tribe, history) ->
            HistoryProps(tribe, history, Controls(commandFunc, reload))
        }
    ), key = tribeId.value)
}
