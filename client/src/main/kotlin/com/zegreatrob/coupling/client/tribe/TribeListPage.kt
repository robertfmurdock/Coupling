package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.couplingDataLoadWrapper
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction

private val LoadedTribeList = couplingDataLoadWrapper(TribeList)

val TribeListPage = reactFunction<PageProps> { props ->
    child(LoadedTribeList, dataLoadProps(
        commander = props.commander,
        query = TribeListQuery,
        toProps = { _, _, tribes -> TribeListProps(tribes, props.pathSetter) }
    ))
}
