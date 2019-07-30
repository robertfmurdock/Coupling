package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignActionDispatcher
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.await
import react.RBuilder
import kotlin.js.Promise


object PlayerPage : ComponentProvider<PageProps>(), PlayerPageBuilder

val RBuilder.playerPage get() = PlayerPage.captor(this)

private val LoadedPlayer = dataLoadWrapper(PlayerConfig)
private val RBuilder.loadedPlayer get() = LoadedPlayer.captor(this)

interface PlayerPageBuilder : ComponentBuilder<PageProps>, FindCallSignActionDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.pathParams["tribeId"]?.first()?.let(::TribeId)
        val playerId = pageProps.pathParams["playerId"]?.first()

        if (tribeId != null) {
            loadedPlayer(DataLoadProps { reload -> pageProps.toPlayerProps(tribeId, playerId, reload) }) {
                playerId?.let { attrs { key = it } }
            }
        } else throw Exception("WHAT")
    }

    private suspend fun PageProps.toPlayerProps(tribeId: TribeId, playerId: String?, reload: () -> Unit) =
            coupling.getData(tribeId)
                    .let { (tribe, players) ->
                        val player = players.findOrDefaultNew(playerId)

                        PlayerConfigProps(
                                tribe = tribe,
                                player = player,
                                players = players,
                                pathSetter = pathSetter,
                                coupling = coupling,
                                reload = reload
                        )
                    }

    private fun List<Player>.findOrDefaultNew(playerId: String?) = firstOrNull { it.id == playerId }
            ?: defaultWithCallSign(this)

    private fun defaultWithCallSign(players: List<Player>) = FindCallSignAction(players, "")
            .perform()
            .let { callSign ->
                Player(
                        callSignAdjective = callSign.adjective,
                        callSignNoun = callSign.noun
                )
            }

    private suspend fun Coupling.getData(tribeId: TribeId) =
            (getTribeAsync(tribeId) to getPlayerListAsync(tribeId))
                    .await()

    private suspend fun Pair<Promise<KtTribe>, Promise<List<Player>>>.await() =
            first.await() to second.await()
}
