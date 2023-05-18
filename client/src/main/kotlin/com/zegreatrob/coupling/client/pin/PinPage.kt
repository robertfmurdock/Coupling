package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.components.pin.PinConfig
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.pinId
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.create

val PinPage = partyPageFunction { props, partyId ->
    val pinId = props.pinId
    +CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                party()
                pinList()
            }
        },
        toDataprops = { reload, commandFunc, result ->
            val pinList = result.partyData?.pinList?.elements ?: return@CouplingQuery null
            PinConfig(
                party = result.partyData?.party?.data ?: return@CouplingQuery null,
                pinList = pinList,
                pin = pinList.firstOrNull { it.id == pinId } ?: Pin(),
                reload = reload,
                dispatchFunc = commandFunc,
            )
        },
    ).create(key = pinId)
}
