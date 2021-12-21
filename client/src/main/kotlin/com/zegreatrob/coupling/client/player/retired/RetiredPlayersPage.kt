package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.minreact.child

private val LoadedRetiredPlayers by lazy { couplingDataLoader<RetiredPlayers>() }

val RetiredPlayersPage = tribePageFunction { props, tribeId ->
    child(dataLoadProps(
        LoadedRetiredPlayers,
        commander = props.commander,
        query = RetiredPlayerListQuery(tribeId),
        toProps = { _, _, (tribe, retiredPlayers) -> RetiredPlayers(tribe, retiredPlayers) }
    ), key = tribeId.value)
}
