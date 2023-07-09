package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.create

val RetiredPlayersPage = partyPageFunction { props, partyId ->
    +CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                party()
                retiredPlayers()
            }
        },
        build = { _, _, result ->
            RetiredPlayers(
                party = result.party?.details?.data ?: return@CouplingQuery,
                retiredPlayers = result.party?.retiredPlayers?.elements ?: return@CouplingQuery,
            )
        },
    ).create(key = partyId.value)
}
