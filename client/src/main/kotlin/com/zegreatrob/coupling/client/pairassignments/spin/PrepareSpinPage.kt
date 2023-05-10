package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.components.spin.PrepareSpin
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.PartyCurrentDataQuery
import com.zegreatrob.minreact.create

val PrepareSpinPage = partyPageFunction { props, partyId ->
    +CouplingQuery(
        commander = props.commander,
        query = PartyCurrentDataQuery(partyId),
        toDataprops = { _, dispatcher, (party, players, currentPairsDoc, pins) ->
            PrepareSpin(party, players, currentPairsDoc, pins, dispatcher)
        },
    ).create(key = partyId.value)
}
