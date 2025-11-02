package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.gql.RetiredPlayersPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.toModel
import js.lazy.Lazy

@Lazy
val RetiredPlayersPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(RetiredPlayersPageQuery(partyId)),
        key = partyId.value.toString(),
    ) { _, _, result ->
        RetiredPlayers(
            party = result.party?.partyDetails?.toModel() ?: return@CouplingQuery,
            retiredPlayers = result.party.retiredPlayers?.map { it.playerDetails.toModel() }
                ?: return@CouplingQuery,
        )
    }
}
