package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.gql.CurrentPairsPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import js.lazy.Lazy
import react.Key

@Lazy
val CurrentPairsPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(CurrentPairsPageQuery(partyId)),
        key = Key(partyId.value.toString()),
    ) { reload, dispatchFunc, result ->
        SocketedPairAssignments(
            party = result.party?.partyDetails?.toDomain()
                ?: return@CouplingQuery,
            boost = result.party.boost?.boostDetails?.toDomain(),
            players = result.party.playerList.map { it.playerDetails.toDomain() },
            pairAssignments = result.party.currentPairingSet?.pairingSetDetails?.toDomain(),
            controls = Controls(dispatchFunc, reload),
            allowSave = false,
        )
    }
}
