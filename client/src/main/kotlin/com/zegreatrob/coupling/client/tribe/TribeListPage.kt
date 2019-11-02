package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.DataLoadProps
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.sdk.AxiosRepositoryCatalog
import com.zegreatrob.coupling.client.sdk.RepositoryCatalog
import react.RBuilder

object TribeListPage : RComponent<PageProps>(provider()), TribeListPageBuilder,
    RepositoryCatalog by AxiosRepositoryCatalog

private val LoadedTribeList = dataLoadWrapper(TribeList)
private val RBuilder.loadedTribeList get() = LoadedTribeList.render(this)

interface TribeListPageBuilder : SimpleComponentRenderer<PageProps>, TribeListQueryDispatcher {

    override fun RContext<PageProps>.render() = reactElement {
        loadedTribeList(
            DataLoadProps {
                val tribes = performTribeQuery()
                TribeListProps(
                    tribes = tribes,
                    pathSetter = props.pathSetter
                )
            }
        )
    }

    private suspend fun performTribeQuery() = TribeListQuery.perform()

}
