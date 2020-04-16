package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder

private val LoadedPlayer = dataLoadWrapper(PlayerConfig)
private val RBuilder.loadedPlayer get() = LoadedPlayer.render(this)

val PlayerPage = reactFunction<PageProps> { props ->
    val tribeId = props.tribeId
    if (tribeId != null) {
        loadedPlayer(tribeId, props)
    } else throw Exception("WHAT")
}

private fun RBuilder.loadedPlayer(tribeId: TribeId, props: PageProps) = with(props) {
    loadedPlayer(dataLoadProps(
        commander = commander,
        query = { TribePlayerQuery(tribeId, playerId).perform() },
        toProps = { reload, commandFunc, (tribe, players, player) ->
            PlayerConfigProps(tribe!!, player, players, pathSetter, reload, commandFunc)
        }
    )) { playerId?.let { attrs { key = it } } }
}
