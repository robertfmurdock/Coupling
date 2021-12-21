package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.tribeId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.child
import react.FC

private val LoadedTribeConfig by lazy { couplingDataLoader<TribeConfig>() }

val TribeConfigPage = FC<PageProps> { props ->
    child(props.tribeId?.tribeQueryProps(props) ?: newTribeProps(props), key = props.tribeId?.value)
}

private fun TribeId.tribeQueryProps(pageProps: PageProps) = dataLoadProps(
    component = LoadedTribeConfig,
    commander = pageProps.commander,
    query = TribeQuery(this),
    toProps = { _, commandFunc, data -> TribeConfig(data, commandFunc) }
)

private fun newTribeProps(pageProps: PageProps) = dataLoadProps(
    component = LoadedTribeConfig,
    commander = pageProps.commander,
    query = NewTribeCommand(),
    toProps = { _, commandFunc, data -> TribeConfig(data, commandFunc) }
)
