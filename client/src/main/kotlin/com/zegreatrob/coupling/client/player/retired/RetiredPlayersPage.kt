package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.minreact.add
import react.key

private val LoadedRetiredPlayers by lazy { couplingDataLoader<RetiredPlayers>() }

val RetiredPlayersPage = partyPageFunction { props, partyId ->
    add(
        dataLoadProps(
            LoadedRetiredPlayers,
            commander = props.commander,
            query = RetiredPlayerListQuery(partyId),
            toProps = { _, _, (party, retiredPlayers) -> RetiredPlayers(party, retiredPlayers) }
        )
    ) {
        key = partyId.value
    }
}
