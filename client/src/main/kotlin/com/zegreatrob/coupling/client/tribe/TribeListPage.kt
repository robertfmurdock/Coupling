package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.child
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.minreact.reactFunction

private val LoadedTribeList = dataLoadWrapper(TribeList)

val TribeListPage = reactFunction<PageProps> { props ->
    child(LoadedTribeList, dataLoadProps(
        commander = props.commander,
        query = TribeListQuery,
        toProps = { _, _, tribes -> TribeListProps(tribes, props.pathSetter) }
    ))
}
