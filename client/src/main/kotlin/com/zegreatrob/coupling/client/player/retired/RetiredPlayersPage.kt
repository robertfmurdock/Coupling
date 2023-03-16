package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.minreact.create

val RetiredPlayersPage = partyPageFunction { props, partyId ->
    +CouplingQuery(
        commander = props.commander,
        query = RetiredPlayerListQuery(partyId),
        toDataprops = { _, _, (party, retiredPlayers) -> RetiredPlayers(party, retiredPlayers) },
    ).create(key = partyId.value)
}
