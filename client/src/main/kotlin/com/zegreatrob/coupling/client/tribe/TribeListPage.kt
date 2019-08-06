package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.*
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
