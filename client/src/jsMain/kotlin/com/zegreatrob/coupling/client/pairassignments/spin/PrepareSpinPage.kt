package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.components.spin.PrepareSpin
import com.zegreatrob.coupling.client.gql.PrepareSpinPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.toModel
import js.lazy.Lazy

@Lazy
val PrepareSpinPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = ApolloGraphQuery(PrepareSpinPageQuery(partyId)),
        key = partyId.value.toString(),
    ) { _, dispatcher, result ->
        val party = result.party
        PrepareSpin(
            party = party?.partyDetails?.toModel() ?: return@CouplingQuery,
            players = party.playerList?.map { it.playerDetailsFragment.toModel() } ?: return@CouplingQuery,
            pins = party.pinList?.map { it.pinDetailsFragment.toModel() } ?: return@CouplingQuery,
            currentPairsDoc = party.currentPairAssignmentDocument?.pairAssignmentDetailsFragment?.toModel(),
            dispatchFunc = dispatcher,
        )
    }
}
