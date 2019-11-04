package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.sdk.SdkSingleton
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.KtTribe
import react.RBuilder
import react.ReactElement

object RetiredPlayersPage : RComponent<PageProps>(provider()), RetiredPlayersPageBuilder,
    RepositoryCatalog by SdkSingleton

private val LoadedRetiredPlayers = dataLoadWrapper(RetiredPlayers)
private val RBuilder.loadedRetiredPlayers get() = LoadedRetiredPlayers.render(this)

interface RetiredPlayersPageBuilder : SimpleComponentRenderer<PageProps>, RetiredPlayerListQueryDispatcher {

    override fun RContext<PageProps>.render(): ReactElement {
        val tribeId = props.tribeId

        return if (tribeId != null) {
            reactElement {
                loadedRetiredPlayers(dataLoadProps(
                    query = { RetiredPlayerListQuery(tribeId).perform() },
                    toProps = { _, data -> toRetiredPlayersProps(data, props.pathSetter) }
                ))
            }
        } else throw Exception("WHAT")
    }

    private fun toRetiredPlayersProps(result: Pair<KtTribe?, List<Player>>, pathSetter: (String) -> Unit) = result
        .let { (tribe, retiredPlayers) ->
            RetiredPlayersProps(
                tribe = tribe!!,
                retiredPlayers = retiredPlayers,
                pathSetter = pathSetter
            )
        }
}
