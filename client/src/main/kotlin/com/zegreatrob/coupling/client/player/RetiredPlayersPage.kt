package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import react.RBuilder


object RetiredPlayersPage : ComponentProvider<PageProps>(), RetiredPlayersPageBuilder

val RBuilder.retiredPlayersPage get() = RetiredPlayersPage.captor(this)

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
