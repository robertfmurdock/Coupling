package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.client.external.react.child
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.tribePageFunction

private val LoadedPairAssignments by lazy { dataLoadWrapper(History) }

val HistoryPage = tribePageFunction { props, tribeId ->
    child(LoadedPairAssignments, dataLoadProps(
        commander = props.commander,
        query = HistoryQuery(tribeId),
        toProps = { reload, commandFunc, (tribe, history) ->
            HistoryProps(tribe!!, history, reload, props.pathSetter, commandFunc)
        }
    ))
}
