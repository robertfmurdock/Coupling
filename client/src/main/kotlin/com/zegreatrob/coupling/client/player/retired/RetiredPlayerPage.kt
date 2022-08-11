package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.player.PlayerConfig
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.partyId
import com.zegreatrob.coupling.client.routing.playerId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.create
import react.ChildrenBuilder
import react.FC
import react.dom.html.ReactHTML.div

val RetiredPlayerPage = FC<PageProps> { props ->
    val partyId = props.partyId
    val playerId = props.playerId

    if (partyId != null && playerId != null)
        loadedRetiredPlayer(props, partyId, playerId)
    else
        div { +"Hey, we're missing the party id or the player id. Things have gone terribly, terribly wrong." }
}

private fun ChildrenBuilder.loadedRetiredPlayer(props: PageProps, partyId: PartyId, playerId: String) =
    +CouplingQuery(
        commander = props.commander,
        query = RetiredPlayerQuery(partyId, playerId),
        toDataprops = { reload, commandFunc, (party, players, player) ->
            PlayerConfig(party, player, players, reload, commandFunc)
        }
    ).create { key = playerId }
