package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.ReloadFunction
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import react.RBuilder


object PlayerPage : ComponentProvider<PageProps>(provider()), PlayerPageBuilder

private val LoadedPlayer = dataLoadWrapper(PlayerConfig)
private val RBuilder.loadedPlayer get() = LoadedPlayer.captor(this)

interface PlayerPageBuilder : SimpleComponentBuilder<PageProps>, PlayerQueryDispatcher {

    override fun build() = buildBy {
        val tribeId = props.tribeId
        val playerId = props.playerId

        if (tribeId != null) {
            reactElement {
                loadedPlayer(
                    dataLoadProps(
                        query = { PlayerQuery(tribeId, playerId).perform() },
                        toProps = toPropsFunc(props)
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
