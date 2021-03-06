package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction

private val LoadedTribeList = couplingDataLoader(TribeList)

val TribeListPage = reactFunction<PageProps> { props ->
    child(LoadedTribeList, dataLoadProps(
        commander = props.commander,
        query = TribeListQuery,
        toProps = { _, _, tribes -> TribeListProps(tribes) }
    ))
}
