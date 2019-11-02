package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.client.sdk.AxiosRepositoryCatalog
import com.zegreatrob.coupling.client.sdk.RepositoryCatalog
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder

object TribeConfigPage : RComponent<PageProps>(provider()), TribeConfigPageBuilder,
    RepositoryCatalog by AxiosRepositoryCatalog

private val LoadedTribeConfig = dataLoadWrapper(TribeConfig)
private val RBuilder.loadedTribeConfig get() = LoadedTribeConfig.render(this)

interface TribeConfigPageBuilder : SimpleComponentRenderer<PageProps>, TribeQueryDispatcher {

    override fun RContext<PageProps>.render() = reactElement {
        loadedTribeConfig(
            dataLoadProps(
                query = { performCorrectQuery(props.tribeId) },
                toProps = { _, data -> tribeConfigProps(data, props.pathSetter) }
            )
        )
    }

    private suspend fun performCorrectQuery(tribeId: TribeId?) = if (tribeId != null)
        TribeQuery(tribeId).perform()
    else
        newTribe()

    private fun newTribe() = KtTribe(
        id = TribeId(""),
        name = "New Tribe",
        defaultBadgeName = "Default",
        alternateBadgeName = "Alternate"
    )

    private fun tribeConfigProps(tribe: KtTribe?, pathSetter: (String) -> Unit) = TribeConfigProps(
        tribe = tribe!!,
        pathSetter = pathSetter
    )
}
