package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.ComponentBuilder
import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.reactFunctionComponent
import com.zegreatrob.coupling.client.routing.DataLoadProps
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder

object TribeListPage : ComponentProvider<PageProps>(), TribeListPageBuilder

private val LoadedTribeList = dataLoadWrapper(TribeList)
private val RBuilder.loadedTribeList get() = LoadedTribeList.captor(this)

interface TribeListPageBuilder : ComponentBuilder<PageProps>, TribeListQueryDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        loadedTribeList(
                DataLoadProps {
                    val tribes = performTribeQuery()
                    TribeListProps(
                            tribes = tribes,
                            pathSetter = pageProps.pathSetter
                    )
                }
        )
    }

    private suspend fun performTribeQuery() = TribeListQuery.perform()

}
