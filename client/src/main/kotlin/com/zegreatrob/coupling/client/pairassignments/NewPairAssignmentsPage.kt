package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.minreact.create

val NewPairAssignmentsPage = partyPageFunction { props, partyId ->
    +CouplingQuery(
        commander = props.commander,
        query = PartyCurrentDataQuery(partyId),
        toDataprops = { reload, commandFunc, (party, players, currentPairsDoc) ->
            SocketedPairAssignments(party, players, currentPairsDoc, Controls(commandFunc, reload), true)
        }
    ).create(key = partyId.value)
}
