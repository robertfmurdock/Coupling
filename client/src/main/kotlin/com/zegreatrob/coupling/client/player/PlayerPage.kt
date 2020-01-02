package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.ReloadFunction
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.sdk.SdkSingleton
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import react.RBuilder
import react.ReactElement

object PlayerPage : RComponent<PageProps>(provider()), PlayerPageBuilder,
    RepositoryCatalog by SdkSingleton

private val LoadedPlayer = dataLoadWrapper(PlayerConfig)
private val RBuilder.loadedPlayer get() = LoadedPlayer.render(this)

interface PlayerPageBuilder : SimpleComponentRenderer<PageProps>, TribePlayerQueryDispatcher {

    override fun RContext<PageProps>.render(): ReactElement {
        val tribeId = props.tribeId
        val playerId = props.playerId

        return if (tribeId != null) {
            reactElement {
                loadedPlayer(
                    dataLoadProps(
                        query = { TribePlayerQuery(tribeId, playerId).perform() },
                        toProps = toPropsFunc(props)
                    )
                ) {
                    playerId?.let { attrs { key = it } }
                }
            }
        } else throw Exception("WHAT")
    }

    private fun toPropsFunc(pageProps: PageProps): (ReloadFunction, Triple<Tribe?, List<Player>, Player>) -> PlayerConfigProps =
        { reload, (tribe, players, player) ->
            PlayerConfigProps(
                tribe = tribe!!,
                player = player,
                players = players,
                pathSetter = pageProps.pathSetter,
                reload = reload
            )
        }
}
