package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder

private val LoadedTribeConfig by lazy { dataLoadWrapper(TribeConfig) }
private val RBuilder.loadedTribeConfig get() = LoadedTribeConfig.render(this)

val TribeConfigPage = reactFunction<PageProps> { props ->
    with(props) {
        loadedTribeConfig(dataLoadProps(
            commander = commander,
            query = { performCorrectQuery(tribeId) },
            toProps = { _, commandFunc, data -> TribeConfigProps(data!!, pathSetter, commandFunc) }
        ))
    }
}

private suspend fun TribeQueryDispatcher.performCorrectQuery(tribeId: TribeId?) = if (tribeId != null)
    TribeQuery(tribeId).perform()
else
    newTribe()

private fun newTribe() = Tribe(
    id = TribeId(""),
    name = "New Tribe",
    defaultBadgeName = "Default",
    alternateBadgeName = "Alternate"
)
