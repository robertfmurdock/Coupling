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
                details()
                playerList()
                currentPairAssignments()
            }
        },
        key = partyId.value.toString(),
    ) { reload, dispatchFunc, result ->
        SocketedPairAssignments(
            party = result.party?.details?.data ?: return@CouplingQuery,
            players = result.party?.playerList?.elements ?: return@CouplingQuery,
            pairAssignments = result.party?.currentPairAssignmentDocument?.element,
            controls = Controls(dispatchFunc, reload),
            allowSave = true,
        )
    }
}
