package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder

private val LoadedTribeList = dataLoadWrapper(TribeList)
private val RBuilder.loadedTribeList get() = LoadedTribeList.render(this)

val TribeListPage = reactFunction<PageProps> { props ->
    with(props) {
        loadedTribeList(
            dataLoadProps(
                commander = props.commander,
                query = TribeListQuery,
                toProps = { _, _, tribes -> TribeListProps(tribes, pathSetter) }
            )
        )
    }
}
