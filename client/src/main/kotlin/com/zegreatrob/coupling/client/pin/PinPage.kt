package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.components.pin.PinConfig
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.pinId
import com.zegreatrob.coupling.sdk.PartyPinQuery
import com.zegreatrob.minreact.create

val PinPage = partyPageFunction { props, partyId ->
    val pinId = props.pinId
    +CouplingQuery(
        commander = props.commander,
        query = PartyPinQuery(partyId, pinId),
        toDataprops = { reload, commandFunc, (party, pins, pin) ->
            PinConfig(party, pin, pins, reload, commandFunc)
        },
    ).create(key = pinId)
}
