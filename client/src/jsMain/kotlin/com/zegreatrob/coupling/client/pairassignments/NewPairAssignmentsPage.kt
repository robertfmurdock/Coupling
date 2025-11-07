package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.gql.NewPairsPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import js.lazy.Lazy

@Lazy
val NewPairAssignmentsPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(NewPairsPageQuery(partyId)),
        key = partyId.value.toString(),
    ) { reload, dispatchFunc, result ->
        SocketedPairAssignments(
            party = result.party?.partyDetails?.toDomain() ?: return@CouplingQuery,
            players = result.party.playerList.map { it.playerDetails.toDomain() },
            pairAssignments = result.party.currentPairAssignmentDocument?.pairAssignmentDetails?.toDomain(),
            controls = Controls(dispatchFunc, reload),
            allowSave = true,
        )
    }
}
