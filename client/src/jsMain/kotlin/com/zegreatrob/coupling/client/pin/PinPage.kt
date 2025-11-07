package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.components.pin.PinConfig
import com.zegreatrob.coupling.client.gql.PinPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.pinId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import js.lazy.Lazy

@Lazy
val PinPage = partyPageFunction { props, partyId ->
    val pinId = props.pinId
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(PinPageQuery(partyId)),
        key = pinId?.value?.toString(),
    ) { reload, commandFunc, result ->
        val pinList = result.party?.pinList?.map { it.pinDetails.toDomain() }
            ?: return@CouplingQuery
        PinConfig(
            party = result.party.partyDetails.toDomain(),
            boost = null,
            pinList = pinList,
            pin = pinList.firstOrNull { it.id == pinId } ?: Pin(id = PinId.new()),
            reload = reload,
            dispatchFunc = commandFunc,
        )
    }
}
