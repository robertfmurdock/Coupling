package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.playerId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.create

val PlayerPage = partyPageFunction { props: PageProps, partyId: PartyId ->
    +CouplingQuery(
        commander = props.commander,
        query = PartyPlayerQuery(partyId, props.playerId),
        toDataprops = { reload, commandFunc, (party, players, player) ->
            PlayerConfig(party, player, players, reload, commandFunc)
        }
    ).create {
        key = "${partyId.value}-${props.playerId}"
    }
}
