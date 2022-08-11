package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.minreact.create

val HistoryPage = partyPageFunction { props, partyId ->
    +CouplingQuery(
        commander = props.commander,
        query = HistoryQuery(partyId),
        toDataprops = { reload, commandFunc, (party, history) ->
            History(party, history, Controls(commandFunc, reload))
        }
    ).create {
        key = partyId.value
    }
}
