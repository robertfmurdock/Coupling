package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.gql.PartyListQuery
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import com.zegreatrob.minreact.nfc
import js.lazy.Lazy

@Lazy
val PartyListPage by nfc<PageProps> { props ->
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(PartyListQuery()),
    ) { _, _, result ->
        PartyList(result.partyList.map { it.partyDetails.toDomain() })
    }
}
