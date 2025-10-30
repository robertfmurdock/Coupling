package com.zegreatrob.coupling.client.incubating

import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.nfc
import js.lazy.Lazy

@Lazy
val IncubatingPage by nfc<PageProps> { props ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            partyList { details() }
            config {
                addToSlackUrl()
                discordClientId()
            }
        },
    ) { _, _, result ->
        val addToSlackUrl = result.config?.addToSlackUrl
        val discordClientId = result.config?.discordClientId
        if (addToSlackUrl != null && discordClientId != null) {
            IncubatingContent(
                discordClientId = discordClientId,
                addToSlackUrl = addToSlackUrl,
                partyList = result.partyList?.mapNotNull { it.details?.data } ?: emptyList(),
            )
        }
    }
}
