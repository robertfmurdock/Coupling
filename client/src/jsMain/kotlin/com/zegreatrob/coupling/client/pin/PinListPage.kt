package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.gql.PinListPageQuery
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import js.lazy.Lazy

@Lazy
val PinListPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(PinListPageQuery(partyId)),
        key = partyId.value.toString(),
    ) { _, _, result ->
        PinList(
            party = result.party?.partyDetails?.toDomain()
                ?: return@CouplingQuery,
            pins = result.party.pinList?.map { it.pinDetails.toDomain() }
                ?: return@CouplingQuery,
        )
    }
}
