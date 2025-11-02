package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.gql.CurrentPairsPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.adapter.toModel
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.toModel
import js.lazy.Lazy

@Lazy
val CurrentPairsPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(CurrentPairsPageQuery(partyId)),
        key = partyId.value.toString(),
    ) { reload, dispatchFunc, result ->
        SocketedPairAssignments(
            party = result.party?.partyDetails?.toModel()
                ?: return@CouplingQuery,
            boost = result.party.boost?.boostDetails?.toModel(),
            players = result.party.playerList?.map { it.playerDetails.toModel() }
                ?: return@CouplingQuery,
            pairAssignments = result.party.currentPairAssignmentDocument?.pairAssignmentDetails?.toModel(),
            controls = Controls(dispatchFunc, reload),
            allowSave = false,
        )
    }
}
