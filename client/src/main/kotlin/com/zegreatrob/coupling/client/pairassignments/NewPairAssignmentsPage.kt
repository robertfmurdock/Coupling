package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.Commander
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.create
import react.key

private val LoadedPairAssignments by lazy { couplingDataLoader<SocketedPairAssignments>() }

val NewPairAssignmentsPage = partyPageFunction { props, partyId ->
    +dataLoadProps(partyId, props.commander).create {
        key = partyId.value
    }
}

private fun dataLoadProps(partyId: PartyId, commander: Commander) = dataLoadProps(
    LoadedPairAssignments,
    commander = commander,
    query = PartyCurrentDataQuery(partyId),
    toProps = { reload, commandFunc, (party, players, currentPairsDoc) ->
        SocketedPairAssignments(party, players, currentPairsDoc, Controls(commandFunc, reload), true)
    }
)
