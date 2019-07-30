package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.player.PageProps
import kotlinx.coroutines.await
import react.RBuilder


object TribeListPage : ComponentProvider<PageProps>(), TribeListPageBuilder

val RBuilder.tribeListPage get() = TribeListPage.captor(this)

private val LoadedTribeList = dataLoadWrapper(TribeList)
private val RBuilder.loadedTribeList get() = LoadedTribeList.captor(this)

interface TribeListPageBuilder : ComponentBuilder<PageProps> {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        loadedTribeList(
                DataLoadProps { pageProps.toTribeListProps() }
        )
    }

    private suspend fun PageProps.toTribeListProps() = coupling.getTribeListAsync()
            .await()
            .let { tribes ->
                TribeListProps(
                        tribes = tribes,
                        pathSetter = pathSetter
                )
            }
}
