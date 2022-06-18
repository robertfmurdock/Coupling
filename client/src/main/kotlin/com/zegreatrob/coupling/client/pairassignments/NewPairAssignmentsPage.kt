package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingDataLoader
import com.zegreatrob.minreact.create
import react.key

val NewPairAssignmentsPage = partyPageFunction { props, partyId ->
    +CouplingDataLoader(
        commander = props.commander,
        query = PartyCurrentDataQuery(partyId),
        toProps = { reload, commandFunc, (party, players, currentPairsDoc) ->
            SocketedPairAssignments(party, players, currentPairsDoc, Controls(commandFunc, reload), true)
        }
    ).create {
        key = partyId.value
    }
}
