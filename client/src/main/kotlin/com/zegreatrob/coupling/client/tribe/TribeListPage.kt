package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import react.fc

private val LoadedTribeList = couplingDataLoader<TribeList>()

val TribeListPage = fc<PageProps> { props ->
    child(dataLoadProps(
        LoadedTribeList,
        commander = props.commander,
        query = TribeListQuery,
        toProps = { _, _, tribes -> TribeList(tribes) }
    ))
}
