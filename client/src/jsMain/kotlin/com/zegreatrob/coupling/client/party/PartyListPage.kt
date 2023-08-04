package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.nfc

val PartyListPage by nfc<PageProps> { props ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery { partyList { details() } },
        toNode = { _, _, result -> PartyList.create(result.partyList?.mapNotNull { it.details?.data } ?: emptyList()) },
    )
}
