package com.zegreatrob.coupling.client.incubating

import com.zegreatrob.coupling.client.gql.IncubatingQuery
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.toModel
import com.zegreatrob.minreact.nfc
import js.lazy.Lazy

@Lazy
val IncubatingPage by nfc<PageProps> { props ->
    CouplingQuery(
        commander = props.commander,
        query = GqlQuery(IncubatingQuery()),
    ) { _, _, result ->
        val addToSlackUrl = result.config?.addToSlackUrl
        val discordClientId = result.config?.discordClientId
        if (addToSlackUrl != null && discordClientId != null) {
            IncubatingContent(
                discordClientId = discordClientId,
                addToSlackUrl = addToSlackUrl,
                partyList = result.partyList?.mapNotNull { it.partyDetails.toModel() } ?: emptyList(),
            )
        }
    }
}
