package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.components.party.PartyConfig
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.partyId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.add
import react.FC

val PartyConfigPage = FC<PageProps> { props ->
    add((props.partyId?.partyQueryProps(props) ?: newPartyProps(props)), key = props.partyId?.value)
}

private fun PartyId.partyQueryProps(pageProps: PageProps) = CouplingQuery(
    commander = pageProps.commander,
    query = PartyQuery(this),
    toDataprops = { _, commandFunc, data -> PartyConfig(data, commandFunc) },
)

private fun newPartyProps(pageProps: PageProps) = CouplingQuery(
    commander = pageProps.commander,
    query = NewPartyCommand(),
    toDataprops = { _, commandFunc, data -> PartyConfig(data, commandFunc) },
)
