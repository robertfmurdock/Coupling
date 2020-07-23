package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.tribePageFunction
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.child
import react.RBuilder

private val LoadedRetiredPlayers by lazy { dataLoadWrapper(RetiredPlayers) }

val RetiredPlayersPage = tribePageFunction { props, tribeId ->
    loadedRetiredPlayers(tribeId, props)
}

private fun RBuilder.loadedRetiredPlayers(tribeId: TribeId, props: PageProps) = with(props) {
    child(LoadedRetiredPlayers, dataLoadProps(
        commander = commander,
        query = RetiredPlayerListQuery(tribeId),
        toProps = { _, _, (tribe, retiredPlayers) -> RetiredPlayersProps(tribe, retiredPlayers, pathSetter) }
    ))
}
