package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.components.party.PartyConfig
import com.zegreatrob.coupling.client.components.party.create
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.partyId
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.nfc

val PartyConfigPage by nfc<PageProps> { props ->
    val partyId = props.partyId
    if (partyId != null) {
        CouplingQuery(
            commander = props.commander,
            query = graphQuery { party(partyId) { party() } },
            toNode = { _, commandFunc, result ->
                PartyConfig.create(
                    party = result.party?.details?.data ?: return@CouplingQuery null,
                    dispatchFunc = commandFunc,
                )
            },
            key = props.partyId?.value,
        )
    } else {
        CouplingQuery(
            commander = props.commander,
            query = graphQuery { user() },
            toNode = { _, commandFunc, _ -> PartyConfig.create(newParty(), commandFunc) },
            key = props.partyId?.value,
        )
    }
}
