package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.components.party.PartyConfig
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.partyId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc

val PartyConfigPage by nfc<PageProps> { props ->
    add((props.partyId?.partyQueryProps(props) ?: newPartyProps(props)), key = props.partyId?.value)
}

private fun PartyId.partyQueryProps(pageProps: PageProps) = CouplingQuery(
    commander = pageProps.commander,
    query = graphQuery { party(this@partyQueryProps) { party() } },
    build = { _, commandFunc, result ->
        PartyConfig(
            party = result.party?.details?.data ?: return@CouplingQuery,
            dispatchFunc = commandFunc,
        )
    },
)

private fun newPartyProps(pageProps: PageProps) = CouplingQuery(
    commander = pageProps.commander,
    query = NewPartyCommand(),
    build = { _, commandFunc, data -> PartyConfig(data, commandFunc) },
)
