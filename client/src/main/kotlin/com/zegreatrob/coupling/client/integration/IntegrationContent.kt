package com.zegreatrob.coupling.client.integration

import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.client.components.integrations.slackChannel
import com.zegreatrob.coupling.client.components.integrations.slackTeam
import com.zegreatrob.coupling.client.incubating.AddToSlackButton
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ntmFC
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.li

data class IntegrationContent(val party: PartyDetails, val integration: PartyIntegration?) :
    DataPropsBind<IntegrationContent>(integrationContent)

val integrationContent by ntmFC<IntegrationContent> { (party, integrations) ->
    div {
        ConfigHeader {
            this.party = party
            +"Integrations!"
        }
        h2 { +"Any integrations enabled for this party will show up here!" }

        if (integrations != null) {
            Editor {
                li { slackTeam(integrations.slackTeam) }
                li { slackChannel(integrations.slackChannel) }
            }
            integrations.slackTeam
        } else {
            +"Looks like this party doesn't have any integrations currently."
        }

        h2 { +"Want to add an integration?" }

        div { AddToSlackButton { url = "" } }
    }
}
