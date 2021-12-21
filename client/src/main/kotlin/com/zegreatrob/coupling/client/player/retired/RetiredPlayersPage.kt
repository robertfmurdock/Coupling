package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder

private val LoadedRetiredPlayers by lazy { couplingDataLoader<RetiredPlayers>() }

val RetiredPlayersPage = tribePageFunction { props, tribeId -> loadedRetiredPlayers(tribeId, props) }

private fun RBuilder.loadedRetiredPlayers(tribeId: TribeId, props: PageProps) = with(props) {
    child(dataLoadProps(
        LoadedRetiredPlayers,
        commander = commander,
        query = RetiredPlayerListQuery(tribeId),
        toProps = { _, _, (tribe, retiredPlayers) -> RetiredPlayers(tribe, retiredPlayers) }
    ), key = tribeId.value)
}
