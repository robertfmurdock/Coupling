package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.partyId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.child
import react.FC

private val LoadedTribeConfig by lazy { couplingDataLoader<TribeConfig>() }

val TribeConfigPage = FC<PageProps> { props ->
    child(props.partyId?.tribeQueryProps(props) ?: newTribeProps(props), key = props.partyId?.value)
}

private fun PartyId.tribeQueryProps(pageProps: PageProps) = dataLoadProps(
    component = LoadedTribeConfig,
    commander = pageProps.commander,
    query = TribeQuery(this),
    toProps = { _, commandFunc, data -> TribeConfig(data, commandFunc) }
)

private fun newTribeProps(pageProps: PageProps) = dataLoadProps(
    component = LoadedTribeConfig,
    commander = pageProps.commander,
    query = NewPartyCommand(),
    toProps = { _, commandFunc, data -> TribeConfig(data, commandFunc) }
)
