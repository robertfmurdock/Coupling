package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.components.party.PartySecretLayout
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.navigateToPartyList
import com.zegreatrob.coupling.client.routing.partyId
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.nfc

val PartySecretsPage by nfc<PageProps> { props ->
    val partyId = props.partyId
    if (partyId == null) {
        +navigateToPartyList()
    } else {
        CouplingQuery(
            commander = props.commander,
            query = graphQuery {
                party(partyId) {
                    details()
                    secretList()
                    boost()
                }
            },
            key = props.partyId?.value?.toString(),
        ) { reload, dispatcher, result ->
            PartySecretLayout(
                partyDetails = result.party?.details?.data ?: return@CouplingQuery,
                secrets = result.party?.secretList?.elements ?: emptyList(),
                boost = result.party?.boost?.data,
                dispatcher = dispatcher,
                reload = reload,
            )
        }
    }
}
