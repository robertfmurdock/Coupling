package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.player.PlayerConfig
import com.zegreatrob.coupling.client.player.PlayerConfigProps
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder
import react.dom.div

private val LoadedRetiredPlayer = couplingDataLoader(PlayerConfig)

val RetiredPlayerPage = reactFunction<PageProps> { props ->
    val tribeId = props.tribeId
    val playerId = props.playerId

    if (tribeId != null && playerId != null)
        loadedRetiredPlayer(props, tribeId, playerId)
    else
        div { +"Hey, we're missing the tribe id or the player id. Things have gone terribly, terribly wrong." }
}

private fun RBuilder.loadedRetiredPlayer(props: PageProps, tribeId: TribeId, playerId: String) =
    child(clazz = LoadedRetiredPlayer, props = dataLoadProps(
        commander = props.commander,
        query = RetiredPlayerQuery(tribeId, playerId),
        toProps = { reload, commandFunc, (tribe, players, player) ->
            PlayerConfigProps(tribe, player, players, reload, commandFunc)
        }
    ), key = playerId)
