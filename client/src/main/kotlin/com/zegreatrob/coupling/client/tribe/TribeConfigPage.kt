package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.await
import react.RBuilder


object TribeConfigPage : ComponentProvider<PageProps>(), TribeConfigPageBuilder

val RBuilder.tribeConfigPage get() = TribeConfigPage.captor(this)

private val LoadedTribeConfig = dataLoadWrapper(TribeConfig)
private val RBuilder.loadedTribeConfig get() = LoadedTribeConfig.captor(this)

interface TribeConfigPageBuilder : ComponentBuilder<PageProps> {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.pathParams["tribeId"]?.let(::TribeId)

        loadedTribeConfig(
                if (tribeId != null)
                    DataLoadProps { pageProps.toTribeConfigProps(tribeId) }
                else
                    DataLoadProps { pageProps.toNewTribeConfigProps() }
        )
    }

    private suspend fun PageProps.toTribeConfigProps(tribeId: TribeId) = coupling.getTribeAsync(tribeId)
            .await()
            .let { tribe ->
                TribeConfigProps(
                        tribe = tribe,
                        pathSetter = pathSetter,
                        coupling = coupling
                )
            }

    private fun PageProps.toNewTribeConfigProps() = TribeConfigProps(
            tribe = KtTribe(
                    id = TribeId(""),
                    name = "New Tribe",
                    defaultBadgeName = "Default",
                    alternateBadgeName = "Alternate"
            ),
            coupling = coupling,
            pathSetter = pathSetter
    )
}
