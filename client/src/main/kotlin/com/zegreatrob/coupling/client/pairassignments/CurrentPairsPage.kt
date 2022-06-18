package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingDataLoader
import com.zegreatrob.minreact.create
import react.key

val CurrentPairsPage = partyPageFunction { props, partyId ->
    +CouplingDataLoader(
        commander = props.commander,
        query = PartyCurrentDataQuery(partyId),
        toProps = { reload, dispatchFunc, (party, players, history) ->
            SocketedPairAssignments(party, players, history, Controls(dispatchFunc, reload), false)
        }
    ).create {
        key = partyId.value
    }
}
