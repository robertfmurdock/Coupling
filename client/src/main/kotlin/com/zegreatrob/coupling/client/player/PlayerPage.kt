package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.external.react.ComponentBuilder
import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.external.react.reactFunctionComponent
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.ReloadFunction
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import react.RBuilder


object PlayerPage : ComponentProvider<PageProps>(), PlayerPageBuilder

private val LoadedPlayer = dataLoadWrapper(PlayerConfig)
private val RBuilder.loadedPlayer get() = LoadedPlayer.captor(this)

interface PlayerPageBuilder : ComponentBuilder<PageProps>, PlayerQueryDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.tribeId
        val playerId = pageProps.playerId

        if (tribeId != null) {
            reactElement {
                loadedPlayer(
                    dataLoadProps(
                        query = { PlayerQuery(tribeId, playerId).perform() },
                        toProps = toPropsFunc(pageProps)
                    )
                ) {
                    playerId?.let { attrs { key = it } }
                }
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
