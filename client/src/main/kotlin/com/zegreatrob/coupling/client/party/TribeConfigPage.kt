package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.routing.CouplingDataLoader
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.partyId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.add
import react.FC
import react.key

val PartyConfigPage = FC<PageProps> { props ->
    add((props.partyId?.partyQueryProps(props) ?: newPartyProps(props))) {
        key = props.partyId?.value
    }
}

private fun PartyId.partyQueryProps(pageProps: PageProps) = CouplingDataLoader(
    commander = pageProps.commander,
    query = PartyQuery(this),
    toProps = { _, commandFunc, data -> PartyConfig(data, commandFunc) }
)

private fun newPartyProps(pageProps: PageProps) = CouplingDataLoader(
    commander = pageProps.commander,
    query = NewPartyCommand(),
    toProps = { _, commandFunc, data -> PartyConfig(data, commandFunc) }
)
