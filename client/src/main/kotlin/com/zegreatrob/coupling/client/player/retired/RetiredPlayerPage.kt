package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.player.PlayerConfig
import com.zegreatrob.coupling.client.player.PlayerConfigProps
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder

private val LoadedRetiredPlayer = dataLoadWrapper(PlayerConfig)
private val RBuilder.loadedRetiredPlayer get() = LoadedRetiredPlayer.render(this)

val RetiredPlayerPage = reactFunction<PageProps> { props ->
    val tribeId = props.tribeId
    val playerId = props.playerId

    if (tribeId != null && playerId != null) {
        loadedRetiredPlayer(props, tribeId, playerId)
    } else throw Exception("WHAT")
}

private fun RBuilder.loadedRetiredPlayer(props: PageProps, tribeId: TribeId, playerId: String) =
    loadedRetiredPlayer(dataLoadProps(
        commander = props.commander,
        query = { RetiredPlayerQuery(tribeId, playerId).perform() },
        toProps = { reload, commandFunc, (tribe, players, player) ->
            PlayerConfigProps(tribe!!, player, players, props.pathSetter, reload, commandFunc)
        }
    )) { attrs { key = playerId } }
