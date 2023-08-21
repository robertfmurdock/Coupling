package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.graphQuery

val CurrentPairsPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = currentPairsQuery(partyId),
        toNode = { reload, dispatchFunc, result ->
            SocketedPairAssignments.create(
                party = result.party?.details?.data ?: return@CouplingQuery null,
                boost = result.party?.boost?.data,
                players = result.party?.playerList?.elements ?: return@CouplingQuery null,
                pairAssignments = result.party?.currentPairAssignmentDocument?.element,
                controls = Controls(dispatchFunc, reload),
                allowSave = false,
            )
        },
        key = partyId.value,
    )
}

private fun currentPairsQuery(partyId: PartyId) = graphQuery {
    party(partyId) {
        details()
        playerList()
        currentPairAssignments()
        boost()
    }
}
