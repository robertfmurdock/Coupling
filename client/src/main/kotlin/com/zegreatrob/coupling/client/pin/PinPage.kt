package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingDataLoader
import com.zegreatrob.coupling.client.routing.pinId
import com.zegreatrob.minreact.create
import react.key

val PinPage = partyPageFunction { props, partyId ->
    val pinId = props.pinId
    +CouplingDataLoader(
        commander = props.commander,
        query = PartyPinQuery(partyId, pinId),
        toProps = { reload, commandFunc, (party, pins, pin) ->
            PinConfig(party, pin, pins, reload, commandFunc)
        }
    ).create {
        key = pinId
    }
}
