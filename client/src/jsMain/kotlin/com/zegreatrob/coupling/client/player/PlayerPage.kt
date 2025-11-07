package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.action.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.client.components.player.PlayerConfig
import com.zegreatrob.coupling.client.gql.PlayerPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.playerId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import js.lazy.Lazy

@Lazy
val PlayerPage = partyPageFunction { props: PageProps, partyId: PartyId ->
    val playerId = props.playerId
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(PlayerPageQuery(partyId)),
        key = "${partyId.value}-$playerId",
    ) { reload, commandFunc, data ->
        val partyDetails = data.party?.partyDetails?.toDomain() ?: return@CouplingQuery
        val playerList = data.party.playerList.map { it.playerDetails.toDomain() }
        val retiredPlayers = data.party.retiredPlayers.map { it.playerDetails.toDomain() }
        val player = (playerList + retiredPlayers).find { it.id == playerId }
            ?: playerList.defaultWithCallSign()
        PlayerConfig(
            party = partyDetails,
            boost = null,
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
    id = PlayerId.new(),
    callSignAdjective = callSign.adjective,
    callSignNoun = callSign.noun,
)
