package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.components.spin.PrepareSpin
import com.zegreatrob.coupling.client.components.spin.create
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
                party()
                playerList()
                currentPairAssignments()
                pinList()
            }
        },
        toNode = { _, dispatcher, result ->
            PrepareSpin.create(
                party = result.party?.details?.data ?: return@CouplingQuery null,
                players = result.party?.playerList?.elements ?: return@CouplingQuery null,
                pins = result.party?.pinList?.elements ?: return@CouplingQuery null,
                currentPairsDoc = result.party?.currentPairAssignmentDocument?.element,
                dispatchFunc = dispatcher,
            )
        },
        key = partyId.value,
    )
}
