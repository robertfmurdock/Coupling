package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.Commander
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.add
import react.key

private val LoadedPairAssignments by lazy { couplingDataLoader<SocketedPairAssignments>() }

val CurrentPairsPage = partyPageFunction { props, partyId ->
    add(dataLoadProps(partyId, props.commander)) {
        key = partyId.value
    }
}

private fun dataLoadProps(partyId: PartyId, commander: Commander) = dataLoadProps(
    LoadedPairAssignments,
    commander = commander,
    query = PartyCurrentDataQuery(partyId),
    toProps = { reload, dispatchFunc, (party, players, history) ->
        SocketedPairAssignments(party, players, history, Controls(dispatchFunc, reload), false)
    }
)
