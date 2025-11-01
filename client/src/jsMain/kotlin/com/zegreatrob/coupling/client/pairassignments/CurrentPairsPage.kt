package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.gql.CurrentPairsPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.adapter.toModel
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.toModel
import js.lazy.Lazy

@Lazy
val CurrentPairsPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = ApolloGraphQuery(CurrentPairsPageQuery(partyId)),
        key = partyId.value.toString(),
    ) { reload, dispatchFunc, result ->
        SocketedPairAssignments(
            party = result.party?.partyDetails?.toModel()
                ?: return@CouplingQuery,
            boost = result.party.boost?.boostDetailsFragment?.toModel(),
            players = result.party.playerList?.map { it.playerDetailsFragment.toModel() }
                ?: return@CouplingQuery,
            pairAssignments = result.party.currentPairAssignmentDocument?.pairAssignmentDetailsFragment?.toModel(),
            controls = Controls(dispatchFunc, reload),
            allowSave = false,
        )
    }
}
