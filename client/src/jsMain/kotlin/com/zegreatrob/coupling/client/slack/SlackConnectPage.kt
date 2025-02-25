package com.zegreatrob.coupling.client.slack

import com.zegreatrob.coupling.client.components.slack.SlackConnectPageContent
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.nfc

val SlackConnectPage by nfc<PageProps> { props ->
    val slackTeam = props.search["slackTeam"] ?: ""
    val slackChannel = props.search["slackChannel"] ?: ""
    SlackConnectPageFrame {
        CouplingQuery(
            commander = props.commander,
            query = graphQuery { partyList { details() } },
        ) { _, dispatch, result ->
            SlackConnectPageContent(
                parties = result.partyList?.mapNotNull { it.details?.data } ?: emptyList(),
                slackTeam = slackTeam,
                slackChannel = slackChannel,
                dispatchFunc = dispatch,
            )
        }
    }
}
