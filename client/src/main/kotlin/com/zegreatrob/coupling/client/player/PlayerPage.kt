package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import react.RBuilder


object PlayerPage : ComponentProvider<PageProps>(), PlayerPageBuilder

val RBuilder.playerPage get() = PlayerPage.captor(this)

private val LoadedPlayer = dataLoadWrapper(PlayerConfig)
private val RBuilder.loadedPlayer get() = LoadedPlayer.captor(this)

interface PlayerPageBuilder : ComponentBuilder<PageProps>, PlayerQueryDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.tribeId
        val playerId = pageProps.playerId

        if (tribeId != null) {
            loadedPlayer(
                    dataLoadProps(
                            query = { PlayerQuery(tribeId, playerId).perform() },
                            toProps = toPropsFunc(pageProps)
                    )
            ) {
                playerId?.let { attrs { key = it } }
            }
        } else throw Exception("WHAT")
    }

    private fun toPropsFunc(pageProps: PageProps): (ReloadFunction, Triple<KtTribe, List<Player>, Player>) -> PlayerConfigProps =
            { reload, (tribe, players, player) ->
                PlayerConfigProps(
                        tribe = tribe,
                        player = player,
                        players = players,
                        pathSetter = pageProps.pathSetter,
                        reload = reload
                )
            }
}
