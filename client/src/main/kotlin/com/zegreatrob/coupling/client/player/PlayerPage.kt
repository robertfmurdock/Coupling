package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.buildCommandFunc
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pairassignments.NullTraceIdProvider
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton
import react.RBuilder
import react.ReactElement

object PlayerPage : RComponent<PageProps>(provider()), PlayerPageBuilder,
    RepositoryCatalog by SdkSingleton

private val LoadedPlayer = dataLoadWrapper(PlayerConfig)
private val RBuilder.loadedPlayer get() = LoadedPlayer.render(this)

interface PlayerPageBuilder : SimpleComponentRenderer<PageProps>, TribePlayerQueryDispatcher, NullTraceIdProvider {

    override fun RContext<PageProps>.render(): ReactElement {
        val tribeId = props.tribeId
        val playerId = props.playerId

        return if (tribeId != null) {
            reactElement {
                loadedPlayer(
                    dataLoadProps(
                        query = { TribePlayerQuery(tribeId, playerId).perform() },
                        toProps = { reload, scope, (tribe, players, player) ->
                            PlayerConfigProps(
                                tribe!!,
                                player,
                                players,
                                props.pathSetter,
                                reload,
                                CommandDispatcher.buildCommandFunc(scope)
                            )
                        }
                    )
                ) {
                    playerId?.let { attrs { key = it } }
                }
            }
        } else throw Exception("WHAT")
    }

}
