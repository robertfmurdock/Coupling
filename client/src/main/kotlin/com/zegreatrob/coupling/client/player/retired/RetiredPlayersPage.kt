package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder

private val LoadedRetiredPlayers by lazy { dataLoadWrapper(RetiredPlayers) }
private val RBuilder.loadedRetiredPlayers get() = LoadedRetiredPlayers.render(this)

val RetiredPlayersPage = reactFunction<PageProps> { props ->
    val tribeId = props.tribeId
    if (tribeId != null) {
        loadedRetiredPlayers(tribeId, props)
    } else throw Exception("WHAT")
}

private fun RBuilder.loadedRetiredPlayers(tribeId: TribeId, props: PageProps) = with(props) {
    loadedRetiredPlayers(dataLoadProps(
        commander = commander,
        query = { RetiredPlayerListQuery(tribeId).perform() },
        toProps = { _, _, (tribe, retiredPlayers) -> RetiredPlayersProps(tribe!!, retiredPlayers, pathSetter) }
    ))
}
