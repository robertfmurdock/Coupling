package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.external.react.ComponentBuilder
import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.reactFunctionComponent
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import react.RBuilder

object RetiredPlayersPage : ComponentProvider<PageProps>(), RetiredPlayersPageBuilder

private val LoadedRetiredPlayers = dataLoadWrapper(RetiredPlayers)
private val RBuilder.loadedRetiredPlayers get() = LoadedRetiredPlayers.captor(this)

interface RetiredPlayersPageBuilder : ComponentBuilder<PageProps>, RetiredPlayerListQueryDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.tribeId

        if (tribeId != null) {
            loadedRetiredPlayers(dataLoadProps(
                query = { RetiredPlayerListQuery(tribeId).perform() },
                toProps = { _, data -> toRetiredPlayersProps(data, pageProps.pathSetter) }
            ))
        } else throw Exception("WHAT")
    }

    private fun toRetiredPlayersProps(result: Pair<KtTribe, List<Player>>, pathSetter: (String) -> Unit) = result
        .let { (tribe, retiredPlayers) ->
            RetiredPlayersProps(
                tribe = tribe,
                retiredPlayers = retiredPlayers,
                pathSetter = pathSetter
            )
        }
}
