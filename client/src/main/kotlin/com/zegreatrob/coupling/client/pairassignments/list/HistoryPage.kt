package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.minreact.add
import react.key

private val LoadedPairAssignments by lazy { couplingDataLoader<History>() }

val HistoryPage = partyPageFunction { props, partyId ->
    add(
        dataLoadProps(
            LoadedPairAssignments,
            commander = props.commander,
            query = HistoryQuery(partyId),
            toProps = { reload, commandFunc, (party, history) ->
                History(party, history, Controls(commandFunc, reload))
            }
        )
    ) {
        key = partyId.value
    }
}
