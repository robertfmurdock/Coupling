package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.await
import react.RBuilder
import kotlin.js.Promise


object RetiredPlayersPage : ComponentProvider<PageProps>(), RetiredPlayersPageBuilder

val RBuilder.loadedRetiredPlayers get() = dataLoadWrapper(retiredPlayers.componentProvider).captor(this)

val RBuilder.retiredPlayersPage get() = RetiredPlayersPage.captor(this)

interface RetiredPlayersPageBuilder : ComponentBuilder<PageProps> {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.pathParams["tribeId"]?.first()?.let(::TribeId)

        if (tribeId != null) {
            loadedRetiredPlayers(DataLoadProps { pageProps.toRetiredPlayersProps(tribeId) })
        } else throw Exception("WHAT")
    }

    private suspend fun PageProps.toRetiredPlayersProps(tribeId: TribeId) = coupling.getData(tribeId)
            .let { (tribe, retiredPlayers) ->
                RetiredPlayersProps(
                        tribe = tribe,
                        retiredPlayers = retiredPlayers,
                        pathSetter = pathSetter
                )
            }

    private suspend fun Coupling.getData(tribeId: TribeId): Pair<KtTribe, List<Player>> =
            (getTribeAsync(tribeId) to getRetiredPlayersAsync(tribeId))
                    .await()

    private suspend fun Pair<Promise<KtTribe>, Promise<List<Player>>>.await() = first.await() to second.await()
}
