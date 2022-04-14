package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.player.PlayerConfig
import com.zegreatrob.coupling.client.routing.*
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.child
import react.ChildrenBuilder
import react.FC
import react.dom.html.ReactHTML.div

private val LoadedRetiredPlayer = couplingDataLoader<PlayerConfig>()

val RetiredPlayerPage = FC<PageProps> { props ->
    val tribeId = props.partyId
    val playerId = props.playerId

    if (tribeId != null && playerId != null)
        loadedRetiredPlayer(props, tribeId, playerId)
    else
        div { +"Hey, we're missing the tribe id or the player id. Things have gone terribly, terribly wrong." }
}

private fun ChildrenBuilder.loadedRetiredPlayer(props: PageProps, tribeId: PartyId, playerId: String) =
    child(dataLoadProps(
        component = LoadedRetiredPlayer,
        commander = props.commander,
        query = RetiredPlayerQuery(tribeId, playerId),
        toProps = { reload, commandFunc, (tribe, players, player) ->
            PlayerConfig(tribe, player, players, reload, commandFunc)
        }
    ), key = playerId)
