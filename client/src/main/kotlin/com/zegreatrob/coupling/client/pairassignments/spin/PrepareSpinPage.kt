package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.components.spin.PrepareSpin
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.element
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.graphQuery
import com.zegreatrob.minreact.create

val PrepareSpinPage = partyPageFunction { props, partyId ->
    +CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                party()
                playerList()
                currentPairAssignments()
                pinList()
            }
        },
        toDataprops = { _, dispatcher, result ->
            PrepareSpin(
                party = result.partyData?.party?.data ?: return@CouplingQuery null,
                players = result.partyData?.playerList?.elements ?: return@CouplingQuery null,
                pins = result.partyData?.pinList?.elements ?: return@CouplingQuery null,
                currentPairsDoc = result.partyData?.currentPairAssignmentDocument?.element,
                dispatchFunc = dispatcher,
            )
        },
    ).create(key = partyId.value)
}
