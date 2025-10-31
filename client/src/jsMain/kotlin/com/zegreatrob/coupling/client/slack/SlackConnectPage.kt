package com.zegreatrob.coupling.client.slack

import com.zegreatrob.coupling.client.components.slack.SlackConnectPageContent
import com.zegreatrob.coupling.client.gql.SlackConnectPageQuery
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.toModel
import com.zegreatrob.minreact.nfc
import js.lazy.Lazy

@Lazy
val SlackConnectPage by nfc<PageProps> { props ->
    val slackTeam = props.search.get("slackTeam") ?: ""
    val slackChannel = props.search.get("slackChannel") ?: ""
    SlackConnectPageFrame {
        CouplingQuery(
            commander = props.commander,
            query = ApolloGraphQuery(SlackConnectPageQuery()),
        ) { _, dispatch, result ->
            SlackConnectPageContent(
                parties = result.partyList?.mapNotNull { it.details?.partyDetailsFragment?.toModel() } ?: emptyList(),
                slackTeam = slackTeam,
                slackChannel = slackChannel,
                dispatchFunc = dispatch,
            )
        }
    }
}
