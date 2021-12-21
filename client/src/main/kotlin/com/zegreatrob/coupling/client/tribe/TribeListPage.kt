package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps

private val LoadedTribeList = couplingDataLoader(TribeList)

val TribeListPage = reactFunction<PageProps> { props ->
    child(LoadedTribeList, dataLoadProps(
        commander = props.commander,
        query = TribeListQuery,
        toProps = { _, _, tribes -> TribeListProps(tribes) }
    ))
}
