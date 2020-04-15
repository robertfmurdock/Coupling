package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pairassignments.NullTraceIdProvider
import com.zegreatrob.coupling.client.routing.DataLoadProps
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton
import react.RBuilder

object TribeListPage : RComponent<PageProps>(provider()), TribeListPageBuilder,
    RepositoryCatalog by SdkSingleton

private val LoadedTribeList = dataLoadWrapper(TribeList)
private val RBuilder.loadedTribeList get() = LoadedTribeList.render(this)

interface TribeListPageBuilder : SimpleComponentRenderer<PageProps>, TribeListQueryDispatcher, NullTraceIdProvider {

    override fun RContext<PageProps>.render() = reactElement {
        loadedTribeList(
            DataLoadProps { _, _ ->
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
