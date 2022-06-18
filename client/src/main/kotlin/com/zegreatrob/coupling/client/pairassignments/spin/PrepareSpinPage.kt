package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.pairassignments.PartyCurrentDataQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.minreact.create
import react.key

private val LoadedPairAssignments by lazy { couplingDataLoader<PrepareSpin>() }

val PrepareSpinPage = partyPageFunction { props, partyId ->
    +dataLoadProps(
        component = LoadedPairAssignments,
        commander = props.commander,
        query = PartyCurrentDataQuery(partyId),
        toProps = { _, dispatcher, (party, players, currentPairsDoc, pins) ->
            PrepareSpin(party, players, currentPairsDoc, pins, dispatcher)
        }
    ).create {
        key = partyId.value
    }
}
