package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.minreact.create

val CurrentPairsPage = partyPageFunction { props, partyId ->
    +CouplingQuery(
        commander = props.commander,
        query = PartyCurrentDataQuery(partyId),
        toDataprops = { reload, dispatchFunc, (party, players, history) ->
            SocketedPairAssignments(party, players, history, Controls(dispatchFunc, reload), false)
        }
    ).create(key = partyId.value)
}
