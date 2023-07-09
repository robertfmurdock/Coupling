package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.create
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.nfc

val PartyListPage by nfc<PageProps> { props ->
    +CouplingQuery(
        commander = props.commander,
        query = graphQuery { partyList() },
        build = { _, _, result -> PartyList(result.partyList?.map(Record<PartyDetails>::data) ?: emptyList()) },
    ).create()
}
