package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.gql.UserPageQuery
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.toModel
import com.zegreatrob.minreact.nfc
import js.lazy.Lazy

@Lazy
val UserPage by nfc<PageProps> {
    CouplingQuery(
        commander = it.commander,
        query = GqlQuery(UserPageQuery()),
    ) { reload, dispatcher, result ->
        UserConfig(
            user = result.user?.details?.userDetails?.toModel(),
            partyList = result.partyList?.mapNotNull(::details) ?: emptyList(),
            dispatcher = dispatcher,
            subscription = null,
            prereleaseUserConfig = null,
            reload = reload,
        )
    }
}

private fun details(list: UserPageQuery.PartyList): PartyDetails? = list.partyDetails.toModel()
