package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.minreact.child
import react.FC

private val LoadedTribeList = couplingDataLoader<TribeList>()

val TribeListPage = FC<PageProps> { props ->
    child(dataLoadProps(
        LoadedTribeList,
        commander = props.commander,
        query = TribeListQuery,
        toProps = { _, _, tribes -> TribeList(tribes) }
    ))
}
