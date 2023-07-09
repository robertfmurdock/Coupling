package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery

val NewPairAssignmentsPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                party()
                playerList()
                currentPairAssignments()
            }
        },
        toNode = { reload, dispatchFunc, result ->
            SocketedPairAssignments.create(
                party = result.party?.details?.data ?: return@CouplingQuery null,
                players = result.party?.playerList?.elements ?: return@CouplingQuery null,
                pairAssignments = result.party?.currentPairAssignmentDocument?.element,
                controls = Controls(dispatchFunc, reload),
                allowSave = true,
            )
        },
        key = partyId.value,
    )
}
