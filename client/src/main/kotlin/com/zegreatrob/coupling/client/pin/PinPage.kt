package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.pinId
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.minreact.child

private val LoadedPin by lazy { couplingDataLoader<PinConfig>() }

val PinPage = partyPageFunction { props, partyId ->
    val pinId = props.pinId
    child(dataLoadProps(
        LoadedPin,
        commander = props.commander,
        query = PartyPinQuery(partyId, pinId),
        toProps = { reload, commandFunc, (party, pins, pin) ->
            PinConfig(party, pin, pins, reload, commandFunc)
        }
    ), key = pinId)
}
