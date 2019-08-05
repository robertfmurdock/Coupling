package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import react.RBuilder


object TribeConfigPage : ComponentProvider<PageProps>(), TribeConfigPageBuilder

val RBuilder.tribeConfigPage get() = TribeConfigPage.captor(this)

private val LoadedTribeConfig = dataLoadWrapper(TribeConfig)
private val RBuilder.loadedTribeConfig get() = LoadedTribeConfig.captor(this)

interface TribeConfigPageBuilder : ComponentBuilder<PageProps>, TribeQueryDispatcher {

    override fun build() = reactFunctionComponent<PageProps> { pageProps ->
        val tribeId = pageProps.tribeId

        loadedTribeConfig(
                if (tribeId != null)
                    DataLoadProps { presentExistingTribe(pageProps, tribeId) }
                else
                    DataLoadProps { presentNewTribe(pageProps) }
        )
    }

    private fun presentNewTribe(pageProps: PageProps) = pageProps.toNewTribeConfigProps()

    private suspend fun presentExistingTribe(pageProps: PageProps, tribeId: TribeId) = pageProps
            .performTribeQuery(tribeId)
            .let { pageProps.toTribeConfigProps(it) }

    private suspend fun PageProps.performTribeQuery(tribeId: TribeId) = TribeQuery(tribeId).perform()

    private fun PageProps.toTribeConfigProps(tribe: KtTribe) = TribeConfigProps(
            tribe = tribe,
            pathSetter = pathSetter,
            coupling = coupling
    )

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
