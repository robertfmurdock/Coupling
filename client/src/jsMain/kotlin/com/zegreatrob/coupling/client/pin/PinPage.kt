package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.components.pin.PinConfig
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.pinId
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.sdk.gql.graphQuery
import kotools.types.text.toNotBlankString
import kotlin.uuid.Uuid

val PinPage = partyPageFunction { props, partyId ->
    val pinId = props.pinId
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            party(partyId) {
                details()
                pinList()
            }
        },
        key = pinId,
    ) { reload, commandFunc, result ->
        val pinList = result.party?.pinList?.elements ?: return@CouplingQuery
        PinConfig(
            party = result.party?.details?.data ?: return@CouplingQuery,
            boost = result.party?.boost?.data,
            pinList = pinList,
            pin = pinList.firstOrNull { it.id.toString() == pinId } ?: Pin(
                id = Uuid.random().toString().toNotBlankString().getOrThrow(),
            ),
            reload = reload,
            dispatchFunc = commandFunc,
        )
    }
}
