package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.player.PlayerConfig
import com.zegreatrob.coupling.client.routing.*
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder
import react.dom.div
import react.fc

private val LoadedRetiredPlayer = couplingDataLoader<PlayerConfig>()

val RetiredPlayerPage = fc<PageProps> { props ->
    val tribeId = props.tribeId
    val playerId = props.playerId

    if (tribeId != null && playerId != null)
        loadedRetiredPlayer(props, tribeId, playerId)
    else
        div { +"Hey, we're missing the tribe id or the player id. Things have gone terribly, terribly wrong." }
}

private fun RBuilder.loadedRetiredPlayer(props: PageProps, tribeId: TribeId, playerId: String) =
    child(props = dataLoadProps(
        component = LoadedRetiredPlayer,
        commander = props.commander,
        query = RetiredPlayerQuery(tribeId, playerId),
        toProps = { reload, commandFunc, (tribe, players, player) ->
            PlayerConfig(tribe, player, players, reload, commandFunc)
        }
    ), key = playerId)
