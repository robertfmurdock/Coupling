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
            user {
                details()
                subscription()
                boost()
            }
            config {
                stripeAdminCode()
                stripePurchaseCode()
            }
            partyList { details() }
        },
        toNode = { _, dispatcher, result ->
            UserConfig.create(
                user = result.user?.details,
                subscription = result.user?.subscription,
                partyList = result.partyList?.mapNotNull(Party::details)?.map(Record<PartyDetails>::data)
                    ?: emptyList(),
                dispatcher = dispatcher,
                stripeAdminCode = result.config?.stripeAdminCode ?: return@CouplingQuery null,
                stripePurchaseCode = result.config?.stripePurchaseCode ?: return@CouplingQuery null,
                boost = result.user?.boost?.data,
            )
        },
    )
}
