package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.components.player.PlayerConfig
import com.zegreatrob.coupling.client.components.player.create
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.partyId
import com.zegreatrob.coupling.client.routing.playerId
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.nfc
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div

val RetiredPlayerPage by nfc<PageProps> { props ->
    val partyId = props.partyId
    val playerId = props.playerId

    if (partyId != null && playerId != null) {
        loadedRetiredPlayer(props, partyId, playerId)
    } else {
        div { +"Hey, we're missing the party id or the player id. Things have gone terribly, terribly wrong." }
    }
}

private fun ChildrenBuilder.loadedRetiredPlayer(props: PageProps, partyId: PartyId, playerId: String) =
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                party()
                retiredPlayers()
            }
        },
        toNode = { reload, commandFunc, result ->
            val players = result.party?.retiredPlayers?.elements ?: return@CouplingQuery null
            PlayerConfig.create(
                party = result.party?.details?.data ?: return@CouplingQuery null,
                player = players.first { it.id == playerId },
                players = players,
                reload = reload,
                dispatchFunc = commandFunc,
            )
        },
        key = playerId,
    )
