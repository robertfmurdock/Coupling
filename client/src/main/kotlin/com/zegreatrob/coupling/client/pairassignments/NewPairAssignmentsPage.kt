package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.create

val NewPairAssignmentsPage = partyPageFunction { props, partyId ->
    +CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                party()
                playerList()
                currentPairAssignments()
            }
        },
        toDataprops = { reload, commandFunc, result ->
            SocketedPairAssignments(
                party = result.partyData?.party?.data ?: return@CouplingQuery null,
                players = result.partyData?.playerList?.elements ?: return@CouplingQuery null,
                pairAssignments = result.partyData?.currentPairAssignmentDocument?.element,
                controls = Controls(commandFunc, reload),
                allowSave = true,
            )
        },
    ).create(key = partyId.value)
}
