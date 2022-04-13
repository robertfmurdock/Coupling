package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.routing.Commander
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.child

private val LoadedPairAssignments by lazy { couplingDataLoader<SocketedPairAssignments>() }

val NewPairAssignmentsPage = tribePageFunction { props, tribeId ->
    child(dataLoadProps(tribeId, props.commander), key = tribeId.value)
}

private fun dataLoadProps(tribeId: PartyId, commander: Commander) = dataLoadProps(
    LoadedPairAssignments,
    commander = commander,
    query = TribeCurrentDataQuery(tribeId),
    toProps = { reload, commandFunc, (tribe, players, currentPairsDoc) ->
        SocketedPairAssignments(tribe, players, currentPairsDoc, Controls(commandFunc, reload), true)
    }
)
