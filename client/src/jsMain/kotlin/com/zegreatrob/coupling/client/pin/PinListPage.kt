package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.gql.PinListPageQuery
import com.zegreatrob.coupling.client.party.toModel
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.toModel
import js.lazy.Lazy

@Lazy
val PinListPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = ApolloGraphQuery(PinListPageQuery(partyId)),
        key = partyId.value.toString(),
    ) { _, _, result ->
        PinList(
            party = result.party?.details?.partyDetailsFragment?.toModel()
                ?: return@CouplingQuery,
            pins = result.party.pinList?.map { it.pinDetailsFragment.toModel() }
                ?: return@CouplingQuery,
        )
    }
}
