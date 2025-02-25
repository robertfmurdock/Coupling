package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.action.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.client.components.player.PlayerConfig
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.playerId
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.sdk.gql.graphQuery
import kotlin.uuid.Uuid

val PlayerPage = partyPageFunction { props: PageProps, partyId: PartyId ->
    val playerId = props.playerId
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                playerList()
                retiredPlayers()
            }
        },
        key = "${partyId.value}-$playerId",
    ) { reload, commandFunc, data ->
        val partyDetails = data.party?.details?.data ?: return@CouplingQuery
        val playerList = data.party?.playerList?.elements ?: return@CouplingQuery
        val retiredPlayers = data.party?.retiredPlayers?.elements ?: return@CouplingQuery
        val player = (playerList + retiredPlayers).find { it.id == playerId }
            ?: playerList.defaultWithCallSign()
        PlayerConfig(
            party = partyDetails,
            boost = data.party?.boost?.data,
            player = player,
            players = playerList,
            reload = reload,
            dispatchFunc = commandFunc,
        )
    }
}

private fun List<Player>.defaultWithCallSign() = object : FindCallSignAction.Dispatcher {}
    .perform(FindCallSignAction(this, ""))
    .let(::defaultWith)

private fun defaultWith(callSign: CallSign) = defaultPlayer.copy(
    id = "${Uuid.random()}",
    callSignAdjective = callSign.adjective,
    callSignNoun = callSign.noun,
)
