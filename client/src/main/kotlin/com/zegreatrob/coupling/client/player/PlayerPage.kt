package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.playerId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.create
import react.key

private val LoadedPlayer = couplingDataLoader<PlayerConfig>()

val PlayerPage = partyPageFunction { props: PageProps, partyId: PartyId ->
    +dataLoadProps(
        LoadedPlayer,
        commander = props.commander,
        query = PartyPlayerQuery(partyId, props.playerId),
        toProps = { reload, commandFunc, (party, players, player) ->
            PlayerConfig(party, player, players, reload, commandFunc)
        }
    ).create {
        key = "${partyId.value}-${props.playerId}"
    }
}
