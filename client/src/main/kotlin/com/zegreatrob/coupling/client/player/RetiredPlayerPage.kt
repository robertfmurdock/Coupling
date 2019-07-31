package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignActionDispatcher
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.await
import react.RBuilder
import kotlin.js.Promise


object RetiredPlayerPage : ComponentProvider<PageProps>(), RetiredPlayerPageBuilder

val RBuilder.retiredPlayerPage get() = RetiredPlayerPage.captor(this)

private val LoadedRetiredPlayer = dataLoadWrapper(PlayerConfig)
private val RBuilder.loadedRetiredPlayer get() = LoadedRetiredPlayer.captor(this)

interface RetiredPlayerPageBuilder : ComponentBuilder<PageProps>, FindCallSignActionDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.pathParams["tribeId"]?.first()?.let(::TribeId)
        val playerId = pageProps.pathParams["playerId"]?.first()

        if (tribeId != null && playerId != null) {
            loadedRetiredPlayer(dataLoadProps(pageProps, tribeId, playerId)) {
                attrs { key = playerId }
            }
        } else throw Exception("WHAT")
    }

    private fun dataLoadProps(pageProps: PageProps, tribeId: TribeId, playerId: String) = DataLoadProps { reload ->
        pageProps.toRetiredPlayerProps(tribeId, playerId, reload)
    }

    private suspend fun PageProps.toRetiredPlayerProps(tribeId: TribeId, playerId: String, reload: () -> Unit) =
            coupling.getData(tribeId)
                    .let { (tribe, players) ->
                        val player = players.first { it.id == playerId }

                        PlayerConfigProps(
                                tribe = tribe,
                                player = player,
                                players = players,
                                pathSetter = pathSetter,
                                coupling = coupling,
                                reload = reload
                        )
                    }

    private suspend fun Coupling.getData(tribeId: TribeId) =
            (getTribeAsync(tribeId) to getRetiredPlayerListAsync(tribeId))
                    .await()

    private suspend fun Pair<Promise<KtTribe>, Promise<List<Player>>>.await() =
            first.await() to second.await()
}
