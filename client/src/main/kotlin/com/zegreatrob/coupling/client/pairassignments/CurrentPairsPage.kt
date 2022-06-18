package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.minreact.add
import react.key

private val LoadedPairAssignments by lazy { couplingDataLoader<SocketedPairAssignments<CommandDispatcher>>() }

val CurrentPairsPage = partyPageFunction { props, partyId ->
    add(
        dataLoadProps(
            component = LoadedPairAssignments,
            commander = props.commander,
            query = PartyCurrentDataQuery(partyId),
            toProps = { reload, dispatchFunc, (party, players, history) ->
                SocketedPairAssignments(party, players, history, Controls(dispatchFunc, reload), false)
            }
        )
    ) {
        key = partyId.value
    }
}
