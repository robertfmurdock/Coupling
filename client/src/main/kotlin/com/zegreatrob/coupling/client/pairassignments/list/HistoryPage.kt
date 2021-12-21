package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.tribePageFunction

private val LoadedPairAssignments by lazy { couplingDataLoader<History>() }

val HistoryPage = tribePageFunction { props, tribeId ->
    child(dataLoadProps(
        LoadedPairAssignments,
        commander = props.commander,
        query = HistoryQuery(tribeId),
        toProps = { reload, commandFunc, (tribe, history) ->
            History(tribe, history, Controls(commandFunc, reload))
        }
    ), key = tribeId.value)
}
