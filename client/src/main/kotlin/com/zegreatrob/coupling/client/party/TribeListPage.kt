package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.minreact.create
import react.FC

private val LoadedPartyList = couplingDataLoader<PartyList>()

val PartyListPage = FC<PageProps> { props ->
    +dataLoadProps(
        LoadedPartyList,
        commander = props.commander,
        query = PartyListQuery,
        toProps = { _, _, parties -> PartyList(parties) }
    ).create()
}
