package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction

private val LoadedTribeConfig by lazy { couplingDataLoader(TribeConfig) }

val TribeConfigPage = reactFunction<PageProps> { props ->
    child(LoadedTribeConfig, props.tribeId?.tribeQueryProps(props) ?: newTribeProps(props), key = props.tribeId?.value)
}

private fun TribeId.tribeQueryProps(pageProps: PageProps) = dataLoadProps(
    commander = pageProps.commander,
    query = TribeQuery(this),
    toProps = { _, commandFunc, data -> TribeConfigProps(data, pageProps.pathSetter, commandFunc) }
)


private fun newTribeProps(pageProps: PageProps) = dataLoadProps(
    commander = pageProps.commander,
    query = NewTribeCommand(),
    toProps = { _, commandFunc, data -> TribeConfigProps(data, pageProps.pathSetter, commandFunc) }
)
