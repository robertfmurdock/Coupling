package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.gql.PartyListQuery
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.toModel
import com.zegreatrob.minreact.nfc
import js.lazy.Lazy

@Lazy
val PartyListPage by nfc<PageProps> { props ->
    CouplingQuery(
        commander = props.commander,
        query = ApolloGraphQuery(PartyListQuery()),
    ) { _, _, result ->
        PartyList(
            result.partyList?.mapNotNull { it.details?.partyDetailsFragment?.toModel() }
                ?: return@CouplingQuery,
        )
    }
}
