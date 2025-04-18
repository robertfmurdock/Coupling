package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.components.spin.PrepareSpin
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery

val PrepareSpinPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                playerList()
                currentPairAssignments()
                pinList()
            }
        },
        key = partyId.value.toString(),
    ) { _, dispatcher, result ->
        PrepareSpin(
            party = result.party?.details?.data ?: return@CouplingQuery,
            players = result.party?.playerList?.elements ?: return@CouplingQuery,
            pins = result.party?.pinList?.elements ?: return@CouplingQuery,
            currentPairsDoc = result.party?.currentPairAssignmentDocument?.element,
            dispatchFunc = dispatcher,
        )
    }
}
