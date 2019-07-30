package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.player.PageProps
import com.zegreatrob.coupling.common.entity.pin.Pin
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.await
import react.RBuilder
import kotlin.js.Promise


object PinListPage : ComponentProvider<PageProps>(), PinListPageBuilder

val RBuilder.pinListPage get() = PinListPage.captor(this)

private val LoadedPinList = dataLoadWrapper(PinList)
private val RBuilder.loadedPinList get() = LoadedPinList.captor(this)

interface PinListPageBuilder : ComponentBuilder<PageProps> {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.pathParams["tribeId"]?.first()?.let(::TribeId)

        if (tribeId != null) {
            loadedPinList(DataLoadProps { pageProps.toPinListProps(tribeId) })
        } else throw Exception("WHAT")
    }

    private suspend fun PageProps.toPinListProps(tribeId: TribeId) = coupling.getData(tribeId)
            .let { (tribe, retiredPlayers) ->
                PinListProps(
                        tribe = tribe,
                        pins = retiredPlayers
                )
            }

    private suspend fun Coupling.getData(tribeId: TribeId) =
            (getTribeAsync(tribeId) to getPinListAsync(tribeId))
                    .await()

    private suspend fun Pair<Promise<KtTribe>, Promise<List<Pin>>>.await() = first.await() to second.await()
}
