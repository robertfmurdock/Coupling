package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.components.player.PlayerConfig
import com.zegreatrob.coupling.client.components.player.create
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.playerId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.PartyPlayerQuery

val PlayerPage = partyPageFunction { props: PageProps, partyId: PartyId ->
    CouplingQuery(
        commander = props.commander,
        query = PartyPlayerQuery(partyId, props.playerId),
        toNode = { reload, commandFunc, (party, players, player) ->
            PlayerConfig.create(party, player, players, reload, commandFunc)
        },
        key = "${partyId.value}-${props.playerId}",
    )
}
