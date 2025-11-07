package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.components.spin.PrepareSpin
import com.zegreatrob.coupling.client.gql.PrepareSpinPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import js.lazy.Lazy

@Lazy
val PrepareSpinPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(PrepareSpinPageQuery(partyId)),
        key = partyId.value.toString(),
    ) { _, dispatcher, result ->
        val party = result.party
        PrepareSpin(
            party = party?.partyDetails?.toDomain() ?: return@CouplingQuery,
            players = party.playerList.map { it.playerDetails.toDomain() },
            pins = party.pinList.map { it.pinDetails.toDomain() },
            currentPairsDoc = party.currentPairAssignmentDocument?.pairAssignmentDetails?.toDomain(),
            dispatchFunc = dispatcher,
        )
    }
}
