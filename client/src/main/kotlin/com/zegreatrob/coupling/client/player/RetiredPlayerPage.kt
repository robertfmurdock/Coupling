package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import react.RBuilder


object RetiredPlayerPage : ComponentProvider<PageProps>(), RetiredPlayerPageBuilder

val RBuilder.retiredPlayerPage get() = RetiredPlayerPage.captor(this)

private val LoadedRetiredPlayer = dataLoadWrapper(PlayerConfig)
private val RBuilder.loadedRetiredPlayer get() = LoadedRetiredPlayer.captor(this)

interface RetiredPlayerPageBuilder : ComponentBuilder<PageProps>, RetiredPlayerQueryDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.tribeId
        val playerId = pageProps.playerId

        if (tribeId != null && playerId != null) {
            loadedRetiredPlayer(
                    dataLoadProps(
                            query = { performRetiredPlayerQuery(tribeId, playerId, pageProps.coupling) },
                            toProps = { reload, (tribe, players, player) ->
                                PlayerConfigProps(
                                        tribe = tribe,
                                        player = player,
                                        players = players,
                                        pathSetter = pageProps.pathSetter,
                                        coupling = pageProps.coupling,
                                        reload = reload
                                )
                            }
                    )
            ) {
                attrs { key = playerId }
            }
        } else throw Exception("WHAT")
    }

    private suspend fun performRetiredPlayerQuery(tribeId: TribeId, playerId: String, coupling: Coupling) =
            RetiredPlayerQuery(tribeId, playerId, coupling).perform()
}
