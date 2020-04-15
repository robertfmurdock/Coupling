package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pairassignments.NullTraceIdProvider
import com.zegreatrob.coupling.client.player.PlayerConfig
import com.zegreatrob.coupling.client.player.PlayerConfigProps
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton
import react.RBuilder
import react.ReactElement

object RetiredPlayerPage : RComponent<PageProps>(provider()), RetiredPlayerPageBuilder,
    RepositoryCatalog by SdkSingleton

private val LoadedRetiredPlayer = dataLoadWrapper(PlayerConfig)
private val RBuilder.loadedRetiredPlayer get() = LoadedRetiredPlayer.render(this)

interface RetiredPlayerPageBuilder : SimpleComponentRenderer<PageProps>, RetiredPlayerQueryDispatcher,
    NullTraceIdProvider {

    override fun RContext<PageProps>.render(): ReactElement {
        val tribeId = props.tribeId
        val playerId = props.playerId

        return if (tribeId != null && playerId != null) {
            reactElement {
                loadedRetiredPlayer(
                    dataLoadProps(
                        query = { performRetiredPlayerQuery(tribeId, playerId) },
                        toProps = { reload, _, (tribe, players, player) ->
                            PlayerConfigProps(tribe!!, player, players, props.pathSetter, reload)
                        }
                    )
                ) {
                    attrs { key = playerId }
                }
            }
        } else throw Exception("WHAT")
    }

    private suspend fun performRetiredPlayerQuery(tribeId: TribeId, playerId: String) =
        RetiredPlayerQuery(tribeId, playerId).perform()
}
