package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.create
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import react.FC

val PartyListPage = FC<PageProps> { props ->
    +CouplingQuery(
        commander = props.commander,
        query = PartyListQuery,
        toDataprops = { _, _, parties -> PartyList(parties) }
    ).create()
}
