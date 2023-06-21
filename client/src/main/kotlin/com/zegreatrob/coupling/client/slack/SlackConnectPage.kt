package com.zegreatrob.coupling.client.slack

import com.zegreatrob.coupling.client.components.slack.SlackConnectPageContent
import com.zegreatrob.coupling.client.create
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.nfc

val SlackConnectPage by nfc<PageProps> { props ->
    SlackConnectPageFrame {
        +CouplingQuery(
            commander = props.commander,
            query = graphQuery { partyList() },
            toDataprops = { _, dispatch, result ->
                SlackConnectPageContent(
                    parties = result.partyList?.map(Record<PartyDetails>::data) ?: emptyList(),
                    slackTeam = "",
                    slackChannel = "",
                    dispatchFunc = dispatch,
                )
            },
        ).create()
    }
}
