package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.action.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.client.components.player.PlayerConfig
import com.zegreatrob.coupling.client.gql.PlayerPageQuery
import com.zegreatrob.coupling.client.party.toModel
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.playerId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.toModel
import js.lazy.Lazy

@Lazy
val PlayerPage = partyPageFunction { props: PageProps, partyId: PartyId ->
    val playerId = props.playerId
    CouplingQuery(
        commander = props.commander,
        query = ApolloGraphQuery(PlayerPageQuery(partyId)),
        key = "${partyId.value}-$playerId",
    ) { reload, commandFunc, data ->
        val partyDetails = data.party?.details?.partyDetailsFragment?.toModel() ?: return@CouplingQuery
        val playerList = data.party.playerList?.map { it.playerDetailsFragment.toModel() } ?: return@CouplingQuery
        val retiredPlayers =
            data.party.retiredPlayers?.map { it.playerDetailsFragment.toModel() } ?: return@CouplingQuery
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
