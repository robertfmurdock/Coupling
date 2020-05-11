package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.model.tribe.TribeId
import react.RBuilder

private val LoadedTribeConfig by lazy { dataLoadWrapper(TribeConfig) }
private val RBuilder.loadedTribeConfig get() = LoadedTribeConfig.render(this)

val TribeConfigPage = reactFunction<PageProps> { props ->
    with(props) {
        loadedTribeConfig(
            tribeId?.tribeQueryProps(this)
                ?: newTribeProps(props)
        )
    }
}

private fun TribeId.tribeQueryProps(
    pageProps: PageProps
) = dataLoadProps(
    commander = pageProps.commander,
    query = TribeQuery(this),
    toProps = { _, commandFunc, data -> TribeConfigProps(data!!, pageProps.pathSetter, commandFunc) }
)


private fun newTribeProps(
    pageProps: PageProps
) = dataLoadProps(
    commander = pageProps.commander,
    query = NewTribeCommand(),
    toProps = { _, commandFunc, data -> TribeConfigProps(data, pageProps.pathSetter, commandFunc) }
)
