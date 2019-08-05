package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.entity.pin.Pin
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import react.RBuilder

object PinListPage : ComponentProvider<PageProps>(), PinListPageBuilder

val RBuilder.pinListPage get() = PinListPage.captor(this)

private val LoadedPinList = dataLoadWrapper(PinList)
private val RBuilder.loadedPinList get() = LoadedPinList.captor(this)

interface PinListPageBuilder : ComponentBuilder<PageProps>, PinListQueryDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.tribeId

        if (tribeId != null) {
            loadedPinList(DataLoadProps {
                tribeId.performPinListQuery()
                        .toPinListProps()
            })
        } else throw Exception("WHAT")
    }

    private fun Pair<KtTribe, List<Pin>>.toPinListProps() = let { (tribe, retiredPlayers) ->
        PinListProps(
                tribe = tribe,
                pins = retiredPlayers
        )
    }

    private suspend fun TribeId.performPinListQuery() = PinListQuery(this)
            .perform()
}
