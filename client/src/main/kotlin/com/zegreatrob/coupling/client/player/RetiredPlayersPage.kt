package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.toPlayer
import com.zegreatrob.coupling.common.toTribe
import kotlinx.coroutines.await
import react.RBuilder
import react.RProps
import kotlin.js.Json
import kotlin.js.Promise


object RetiredPlayersPage : ComponentProvider<PageProps>(), RetiredPlayersPageBuilder

val RBuilder.loadedRetiredPlayers get() = dataLoadWrapper(retiredPlayers.componentProvider).captor(this)

val RBuilder.retiredPlayersPage get() = RetiredPlayersPage.captor(this)

data class PageProps(
        val coupling: dynamic,
        val params: Map<String, List<String>>,
        val pathSetter: (String) -> Unit
) : RProps

interface RetiredPlayersPageBuilder : ComponentBuilder<PageProps> {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.params["tribeId"]?.first()

        if (tribeId != null) {
            loadedRetiredPlayers(DataLoadProps { pageProps.toRetiredPlayersProps(tribeId) })
        } else throw Exception("WHAT")
    }

    private suspend fun PageProps.toRetiredPlayersProps(tribeId: String): RetiredPlayersProps {
        console.log("loading data")
        val (tribe, retiredPlayers) = (getTribeAsync(tribeId) to getRetiredPlayersAsync(tribeId))
                .await()
        return RetiredPlayersProps(
                tribe = tribe,
                retiredPlayers = retiredPlayers,
                pathSetter = pathSetter
        )
    }

    private suspend fun Pair<Promise<Json>, Promise<Array<Json>>>.await() =
            first.await().toTribe() to
                    second.await().map { it.toPlayer() }

    private fun PageProps.getTribeAsync(tribeId: String): Promise<Json> =
            coupling.getTribe(tribeId)
                    .unsafeCast<Promise<Json>>()

    private fun PageProps.getRetiredPlayersAsync(tribeId: String): Promise<Array<Json>> =
            coupling.getRetiredPlayers(tribeId)
                    .unsafeCast<Promise<Array<Json>>>()
}
