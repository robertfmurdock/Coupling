package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.components.party.PartyConfig
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.partyId
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.nfc

val PartyConfigPage by nfc<PageProps> { props ->
    val partyId = props.partyId
    if (partyId != null) {
        CouplingQuery(
            commander = props.commander,
            query = graphQuery { party(partyId) { details() } },
            key = props.partyId?.value,
        ) { _, commandFunc, result ->
            PartyConfig(
                party = result.party?.details?.data ?: return@CouplingQuery,
                boost = result.party?.boost?.data,
                dispatchFunc = commandFunc,
            )
        }
    } else {
        CouplingQuery(
            commander = props.commander,
            query = graphQuery { user { details() } },
            key = props.partyId?.value,
        ) { _, commandFunc, _ -> PartyConfig(newParty(), null, commandFunc) }
    }
}

fun newParty() = PartyDetails(
    id = PartyId(""),
    defaultBadgeName = "Default",
    alternateBadgeName = "Alternate",
    name = "New Party",
)
