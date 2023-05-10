package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.action.PartyListQuery
import com.zegreatrob.coupling.client.create
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.minreact.nfc

val PartyListPage by nfc<PageProps> { props ->
    +CouplingQuery(
        commander = props.commander,
        query = PartyListQuery,
        toDataprops = { _, _, parties -> PartyList(parties) },
    ).create()
}
