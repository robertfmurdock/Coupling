package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.Party
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.nfc

val UserPage by nfc<PageProps> {
    CouplingQuery(
        commander = it.commander,
        query = graphQuery {
            user { details() }
            partyList { details() }
        },
    ) { reload, dispatcher, result ->
        UserConfig(
            user = result.user?.details,
            partyList = result.partyList?.mapNotNull(Party::details)?.map(Record<PartyDetails>::data) ?: emptyList(),
            dispatcher = dispatcher,
            subscription = null,
            prereleaseUserConfig = null,
            reload = reload,
        )
    }
}
