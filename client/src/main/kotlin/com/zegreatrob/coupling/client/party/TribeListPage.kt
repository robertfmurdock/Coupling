package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.create
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.sdk.GraphQuery
import com.zegreatrob.coupling.sdk.Queries
import com.zegreatrob.minreact.nfc

val PartyListPage by nfc<PageProps> { props ->
    +CouplingQuery(
        commander = props.commander,
        query = GraphQuery(Queries.listParties),
        toDataprops = { _, _, result -> PartyList(result.partyList?.map(Record<Party>::data) ?: emptyList()) },
    ).create()
}
