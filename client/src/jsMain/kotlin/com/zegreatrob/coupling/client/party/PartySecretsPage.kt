package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.components.party.PartySecretLayout
import com.zegreatrob.coupling.client.gql.PartySecretsPageQuery
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.navigateToPartyList
import com.zegreatrob.coupling.client.routing.partyId
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.minreact.nfc
import js.lazy.Lazy

@Lazy
val PartySecretsPage by nfc<PageProps> { props ->
    val partyId = props.partyId
    if (partyId == null) {
        +navigateToPartyList()
    } else {
        CouplingQuery(
            commander = props.commander,
            query = ApolloGraphQuery(PartySecretsPageQuery(partyId)),
            key = props.partyId?.value?.toString(),
        ) { reload, dispatcher, result ->
            PartySecretLayout(
                partyDetails = result.party?.details?.partyDetailsFragment?.toModel() ?: return@CouplingQuery,
                secrets = result.party.secretList?.map { it.partySecretFragment.toModel() } ?: emptyList(),
                boost = result.party.boost?.boostDetailsFragment?.toModel(),
                dispatcher = dispatcher,
                reload = reload,
            )
        }
    }
}
